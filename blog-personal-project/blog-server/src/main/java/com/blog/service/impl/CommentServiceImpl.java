package com.blog.service.impl;

import com.blog.constant.*;
import com.blog.context.BaseContext;
import com.blog.exception.CommentException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.CommentStatusDTO;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.CommentVo;
import com.blog.result.PageResult;
import com.blog.service.CommentService;
import com.blog.service.NoticeService;
import com.blog.utils.CommentTreeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公共评论服务实现
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询评论列表
     *
     * @param commentPageQueryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(CommentPageQueryDTO commentPageQueryDTO) {
        if (commentPageQueryDTO.getType() == null) {
            throw new CommentException("评论类型不能为空");
        }
        Integer page = commentPageQueryDTO.getPage() == null ? 1 : commentPageQueryDTO.getPage();
        Integer pageSize = commentPageQueryDTO.getPageSize() == null ? 10 : commentPageQueryDTO.getPageSize();
        boolean hasKeyword = commentPageQueryDTO.getKeyword() != null
                && !commentPageQueryDTO.getKeyword().trim().isEmpty();
        // 有关键字时按平铺搜索，方便精确找到某条评论
        if (hasKeyword) {
            return pageQueryFlat(commentPageQueryDTO, page, pageSize);
        }
        // 无关键字时按主楼分页，并带出主楼下全部回复，保证层级完整
        return pageQueryTree(commentPageQueryDTO, page, pageSize);
    }

    /**
     * 分页查询逻辑删除的评论（回收站）
     *
     * @param commentPageQueryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult recyclePageQuery(CommentPageQueryDTO commentPageQueryDTO) {
        if (commentPageQueryDTO.getType() == null) {
            throw new CommentException("评论类型不能为空");
        }
        PageHelper.startPage(commentPageQueryDTO.getPage(), commentPageQueryDTO.getPageSize());
        List<CommentVo> commentList = commentMapper.recyclePageQuery(commentPageQueryDTO);
        PageInfo<CommentVo> pageInfo = new PageInfo<>(commentList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 平铺分页(关键字搜索场景)，并补齐回复对应的主楼上下文
     */
    private PageResult pageQueryFlat(CommentPageQueryDTO dto, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<CommentVo> commentList = commentMapper.pageQuery(dto);
        PageInfo<CommentVo> pageInfo = new PageInfo<>(commentList);

        List<CommentVo> rows = new ArrayList<>(pageInfo.getList());
        // 补齐回复的主楼(可能不在本页)，保证层级完整
        Set<Long> seenIds = rows.stream().map(CommentVo::getId).collect(Collectors.toSet());
        Set<Long> missingIds = collectMissingParentIds(rows, seenIds);
        while (!missingIds.isEmpty()) {
            List<CommentVo> parents = commentMapper.selectByIds(new ArrayList<>(missingIds));
            for (CommentVo parent : parents) {
                if (seenIds.add(parent.getId())) {
                    rows.add(parent);
                }
            }
            missingIds = collectMissingParentIds(rows, seenIds);
        }
        return new PageResult(pageInfo.getTotal(), rows);
    }

    /**
     * 按主楼分页：主楼分页 + 带出主楼下全部回复
     */
    private PageResult pageQueryTree(CommentPageQueryDTO dto, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Long> mainIds = commentMapper.pageMainIds(dto);
        PageInfo<Long> pageInfo = new PageInfo<>(mainIds);
        if (mainIds == null || mainIds.isEmpty()) {
            return new PageResult(pageInfo.getTotal(), new ArrayList<>());
        }

        List<CommentVo> rows = new ArrayList<>(commentMapper.selectByIds(mainIds));
        rows.addAll(commentMapper.selectRepliesByParentIds(mainIds, dto.getStatus()));
        return new PageResult(pageInfo.getTotal(), rows);
    }

    /**
     * 收集当前行集合中回复的缺失父评论ID
     */
    private Set<Long> collectMissingParentIds(List<CommentVo> rows, Set<Long> seenIds) {
        return rows.stream()
                .filter(row -> row.getParentId() != null && row.getParentId() != 0)
                .map(CommentVo::getParentId)
                .filter(parentId -> !seenIds.contains(parentId))
                .collect(Collectors.toSet());
    }

    /**
     * 审核/隐藏评论
     *
     * @param commentStatusDTO 评论ID与目标状态
     */
    @Override
    public void updateStatus(CommentStatusDTO commentStatusDTO) {
        Comment comment = commentMapper.getById(commentStatusDTO.getId());
        if (comment == null || DelStatusConstant.DISABLE.equals(comment.getDeleteFlag())) {
            throw new CommentException("评论不存在");
        }
        if (!StatusConstant.ENABLE.equals(commentStatusDTO.getStatus())
                && !StatusConstant.DISABLE.equals(commentStatusDTO.getStatus())) {
            throw new CommentException("评论状态不合法");
        }
        Comment update = Comment.builder()
                .id(commentStatusDTO.getId())
                .status(commentStatusDTO.getStatus())
                .updateTime(LocalDateTime.now())
                .build();
        commentMapper.update(update);
    }

    /**
     * 后台回复评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReply(CommentReplyDTO commentReplyDTO) {
        Comment parent = commentMapper.getById(commentReplyDTO.getParentId());
        if (parent == null || DelStatusConstant.DISABLE.equals(parent.getDeleteFlag())) {
            throw new CommentException("回复的评论不存在");
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null) {
            throw new CommentException("当前用户不存在或登录已失效");
        }

        Comment reply = Comment.builder()
                .type(parent.getType())
                .sourceId(parent.getSourceId())
                .msgType(parent.getMsgType() == null ? 0 : parent.getMsgType())
                .parentId(parent.getId())
                .replyUserId(parent.getUserId())
                .replyUserNickname(parent.getUserNickname())
                .userId(user.getId())
                .userNickname(user.getNickname())
                .userAvatar(user.getAvatar())
                .content(commentReplyDTO.getContent().trim())
                .likeNum(0)
                .status(StatusConstant.ENABLE)
                .isTop(StatusConstant.DISABLE)
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        commentMapper.add(reply);
        notifyAdminForComment(reply, null);
    }

    /**
     * 发表文章评论
     *
     * @param articleId 文章ID
     * @param content   评论内容
     * @return 新评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticleComment(Long articleId, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new CommentException("评论内容不能为空");
        }
        if (trimmed.length() > 500) {
            throw new CommentException("评论内容最多500字");
        }

        Article article = articleMapper.getArticleById(articleId);
        if (article == null || DelStatusConstant.DISABLE.equals(article.getDeleteFlag())) {
            throw new CommentException("文章不存在或已删除");
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null) {
            throw new CommentException("当前用户不存在或登录已失效");
        }

        Comment comment = Comment.builder()
                .type(0)
                .sourceId(articleId)
                .msgType(0)
                .parentId(LayoutConstant.PARENTID)
                .replyUserId(LayoutConstant.REPLYID)
                .userId(user.getId())
                .userNickname(user.getNickname())
                .userAvatar(user.getAvatar())
                .content(trimmed)
                .likeNum(0)
                .status(StatusConstant.ENABLE)
                .isTop(StatusConstant.DISABLE)
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        commentMapper.add(comment);
        notifyAdminForComment(comment, article.getTitle());
    }

    /**
     * 非博主的用户发表/回复文章评论时，记录通知并推送管理员
     *
     * @param comment        新评论
     * @param fallbackTitle  文章标题（回复场景可传空，由内部补充）
     */
    private void notifyAdminForComment(Comment comment, String fallbackTitle) {
        if (comment == null || !CommentConstant.ZERO.equals(comment.getType())) {
            return;
        }
        // 博主本人评论不通知
        if (sysUserRoleMapper.hasRole(comment.getUserId(), SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }

        String articleTitle = fallbackTitle;
        if (articleTitle == null || articleTitle.trim().isEmpty()) {
            Article article = articleMapper.getArticleById(comment.getSourceId());
            articleTitle = article == null ? null : article.getTitle();
        }
        String operatorName = (comment.getUserNickname() == null || comment.getUserNickname().trim().isEmpty())
                ? "匿名用户" : comment.getUserNickname();

        noticeService.createNotice(
                "comment",
                "收到新评论",
                "评论",
                articleTitle == null || articleTitle.trim().isEmpty() ? "文章" : articleTitle,
                comment.getSourceId(),
                operatorName,
                comment.getContent());
    }

    /**
     * 置顶/取消置顶评论
     *
     * @param id 评论ID
     */
    @Override
    public void updateTop(Long id) {
        Comment comment = commentMapper.getById(id);
        if (comment == null || DelStatusConstant.DISABLE.equals(comment.getDeleteFlag())) {
            throw new CommentException("评论不存在");
        }
        Integer newTop = StatusConstant.ENABLE.equals(comment.getIsTop())
                ? StatusConstant.DISABLE : StatusConstant.ENABLE;
        Comment update = Comment.builder()
                .id(id)
                .isTop(newTop)
                .updateTime(LocalDateTime.now())
                .build();
        commentMapper.update(update);
    }

    /**
     * 批量逻辑删除评论
     *
     * @param ids 评论ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CommentException("请选择要删除的评论");
        }
        commentMapper.logicDelete(ids);
    }

    /**
     * 批量物理删除评论
     *
     * @param ids 评论ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CommentException("请选择要删除的评论");
        }
        commentMapper.delete(ids);
    }

    /**
     * 批量恢复（回收站 -> 正常列表）
     *
     * @param ids 评论ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recover(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CommentException("请选择要恢复的评论");
        }
        commentMapper.recover(ids);
    }

    /**
     * 根据文章id查询文章评论
     * @param id
     * @return
     */
    @Override
    public List<CommentVo> getArticleById(Long id) {
        //构建查询数据对象
        Comment comment = Comment.builder().type(0).sourceId(id).build();
        List<Comment> commentList = commentMapper.getArticle(comment);
        if(commentList == null || commentList.isEmpty()){
            return Collections.emptyList();
        }

        List<CommentVo> commentVoList = commentList.stream()
                .map(entity -> {
                    CommentVo vo = new CommentVo();
                    BeanUtils.copyProperties(entity, vo);
                    boolean isAdmin = sysUserRoleMapper.hasRole(entity.getUserId(), SystemConstant.SUPER_ADMIN_ROLE);
                    vo.setAdmin(isAdmin);
                    return vo;
                }).collect(Collectors.toList());

        //扁平数据转树形结构
        List<CommentVo> commentVos = CommentTreeUtil.buildFlatReplyTree(commentVoList);
        return commentVos;
    }
}
