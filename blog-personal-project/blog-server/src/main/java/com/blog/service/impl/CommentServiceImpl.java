package com.blog.service.impl;

import com.blog.constant.*;
import com.blog.context.BaseContext;
import com.blog.exception.CommentException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.DailyMapper;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.dto.CommentPageQueryDTO;
import com.blog.pojo.dto.CommentReplyDTO;
import com.blog.pojo.dto.CommentStatusDTO;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.vo.CommentVo;
import com.blog.pojo.vo.DailyFrontVO;
import com.blog.result.PageResult;
import com.blog.service.CommentService;
import com.blog.service.NoticeService;
import com.blog.utils.CommentTreeUtil;
import com.blog.utils.NoticeTextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    private DailyMapper dailyMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private LikeMapper likeMapper;

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
     * 分页查询用户端留言板
     *
     * @param commentPageQueryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult pageUserMessageQuery(CommentPageQueryDTO commentPageQueryDTO) {
        commentPageQueryDTO.setType(CommentConstant.TWO);
        // 用户端只展示已审核可见的留言
        commentPageQueryDTO.setStatus(StatusConstant.ENABLE);
        Integer page = commentPageQueryDTO.getPage() == null ? 1 : commentPageQueryDTO.getPage();
        Integer pageSize = commentPageQueryDTO.getPageSize() == null ? 10 : commentPageQueryDTO.getPageSize();

        PageHelper.startPage(page, pageSize);
        List<Long> mainIds = commentMapper.pageMainIds(commentPageQueryDTO);
        PageInfo<Long> pageInfo = new PageInfo<>(mainIds);
        if (mainIds == null || mainIds.isEmpty()) {
            return new PageResult(pageInfo.getTotal(), new ArrayList<>());
        }

        List<CommentVo> rows = new ArrayList<>(commentMapper.selectPublicByIds(mainIds));
        rows.addAll(commentMapper.selectPublicRepliesByParentIds(mainIds, StatusConstant.ENABLE));

        List<CommentVo> messageTree = CommentTreeUtil.buildFlatReplyTree(rows);
        for (CommentVo comment : messageTree) {
            markAdminRecursively(comment);
        }
        fillLikedStatus(messageTree);
        return new PageResult(pageInfo.getTotal(), messageTree);
    }

    /**
     * 递归标记留言及其回复是否为博主
     */
    private void markAdminRecursively(CommentVo comment) {
        comment.setAdmin(sysUserRoleMapper.hasRole(
                comment.getUserId(), SystemConstant.SUPER_ADMIN_ROLE));
        if (comment.getReplies() != null) {
            for (CommentVo reply : comment.getReplies()) {
                markAdminRecursively(reply);
            }
        }
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
        addReply(commentReplyDTO, false);
    }

    /**
     * 用户端回复可见评论
     *
     * @param commentReplyDTO 被回复评论ID与回复内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserReply(CommentReplyDTO commentReplyDTO) {
        addReply(commentReplyDTO, true);
    }

    private void addReply(CommentReplyDTO commentReplyDTO, boolean validateVisibility) {
        Comment parent = commentMapper.getById(commentReplyDTO.getParentId());
        if (parent == null || DelStatusConstant.DISABLE.equals(parent.getDeleteFlag())) {
            throw new CommentException("回复的评论不存在");
        }
        if (validateVisibility) {
            validateVisibleReplyChain(parent);
            if (CommentConstant.ONE.equals(parent.getType())
                    && dailyMapper.getDetailById(parent.getSourceId()) == null) {
                throw new CommentException("日常不存在或暂未公开");
            }
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null) {
            throw new CommentException("当前用户不存在或登录已失效");
        }

        Comment reply = Comment.builder()
                .type(parent.getType())
                .sourceId(parent.getSourceId())
                .msgType(parent.getMsgType() == null ? CommentConstant.ZERO : parent.getMsgType())
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
        if (validateVisibility) {
            notifyAdminForComment(reply, null);
        }
    }

    /**
     * 用户端回复时校验目标评论到顶级评论的完整可见链路
     */
    private void validateVisibleReplyChain(Comment target) {
        Integer type = target.getType();
        Long sourceId = target.getSourceId();
        Comment current = target;
        Set<Long> visitedIds = new HashSet<>();

        while (current != null) {
            if (current.getId() == null || !visitedIds.add(current.getId())) {
                throw new CommentException("评论回复关系异常");
            }
            if (DelStatusConstant.DISABLE.equals(current.getDeleteFlag())
                    || !StatusConstant.ENABLE.equals(current.getStatus())) {
                throw new CommentException("回复的评论已被隐藏");
            }
            if (!type.equals(current.getType()) || !sourceId.equals(current.getSourceId())) {
                throw new CommentException("评论回复关系异常");
            }

            Long parentId = current.getParentId();
            if (parentId == null || LayoutConstant.PARENTID.equals(parentId)) {
                return;
            }
            current = commentMapper.getById(parentId);
            if (current == null) {
                throw new CommentException("回复的评论不存在");
            }
        }
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
                .type(CommentConstant.ZERO)
                .sourceId(articleId)
                .msgType(CommentConstant.ZERO)
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
     * 发表日常顶级评论
     *
     * @param dailyId 日常ID
     * @param content 评论内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDailyComment(Long dailyId, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new CommentException("评论内容不能为空");
        }
        if (trimmed.length() > 500) {
            throw new CommentException("评论内容最多500字");
        }

        DailyFrontVO daily = dailyMapper.getDetailById(dailyId);
        if (daily == null) {
            throw new CommentException("日常不存在或暂未公开");
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null) {
            throw new CommentException("当前用户不存在或登录已失效");
        }

        Comment comment = Comment.builder()
                .type(CommentConstant.ONE)
                .sourceId(dailyId)
                .msgType(CommentConstant.ZERO)
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
        notifyAdminForComment(comment,
                NoticeTextUtil.dailySummary(daily.getContent(), daily.getId()));
    }

    /**
     * 发表留言板留言
     *
     * @param msgType 留言类型 0评论留言 1反馈建议 2申请友链
     * @param content 留言内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMessageComment(Integer msgType, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new CommentException("留言内容不能为空");
        }
        if (trimmed.length() > 500) {
            throw new CommentException("留言内容最多500字");
        }

        Integer normalizedMsgType = msgType == null ? CommentConstant.ZERO : msgType;
        if (normalizedMsgType < CommentConstant.ZERO || normalizedMsgType > CommentConstant.TWO) {
            throw new CommentException("留言类型不合法");
        }

        SysUser user = sysUserMapper.getByUserId(BaseContext.getCurrentId());
        if (user == null) {
            throw new CommentException("当前用户不存在或登录已失效");
        }

        Comment comment = Comment.builder()
                .type(CommentConstant.TWO)
                .sourceId(LayoutConstant.PARENTID)
                .msgType(normalizedMsgType)
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
        notifyAdminForComment(comment, null);
    }

    /**
     * 非博主用户发表或回复公开评论时，记录通知并推送管理员
     *
     * @param comment             新评论
     * @param fallbackTargetTitle 目标标题或摘要（回复场景可传空，由内部补充）
     */
    private void notifyAdminForComment(Comment comment, String fallbackTargetTitle) {
        if (comment == null
                || (!CommentConstant.ZERO.equals(comment.getType())
                && !CommentConstant.ONE.equals(comment.getType())
                && !CommentConstant.TWO.equals(comment.getType()))) {
            return;
        }
        // 博主本人评论不通知
        if (sysUserRoleMapper.hasRole(comment.getUserId(), SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }

        String targetType;
        String targetTitle = fallbackTargetTitle;
        Long targetId = comment.getSourceId();
        if (CommentConstant.ZERO.equals(comment.getType())) {
            targetType = NoticeConstant.TARGET_ARTICLE;
            if (targetTitle == null || targetTitle.trim().isEmpty()) {
                Article article = articleMapper.getArticleById(comment.getSourceId());
                targetTitle = article == null ? null : article.getTitle();
            }
            if (targetTitle == null || targetTitle.trim().isEmpty()) {
                targetTitle = "文章 #" + comment.getSourceId();
            }
        } else if (CommentConstant.ONE.equals(comment.getType())) {
            targetType = NoticeConstant.TARGET_DAILY;
            if (targetTitle == null || targetTitle.trim().isEmpty()) {
                DailyFrontVO daily = dailyMapper.getDetailById(comment.getSourceId());
                targetTitle = NoticeTextUtil.dailySummary(
                        daily == null ? null : daily.getContent(), comment.getSourceId());
            }
        } else {
            targetType = NoticeConstant.TARGET_MESSAGE;
            // 留言板没有具体来源ID，使用留言自身ID便于后台跳转定位
            targetId = comment.getId();
            if (targetTitle == null || targetTitle.trim().isEmpty()) {
                targetTitle = messageSummary(comment.getContent());
            }
        }

        boolean isReply = comment.getParentId() != null
                && !LayoutConstant.PARENTID.equals(comment.getParentId());
        boolean isMessage = CommentConstant.TWO.equals(comment.getType());
        String title = isReply ? "收到新回复" : (isMessage ? "收到新留言" : "收到新评论");
        String actionText = isReply ? "回复评论" : "评论";
        if (isMessage) {
            actionText = isReply ? "回复留言" : "留言";
        }
        String operatorName = (comment.getUserNickname() == null
                || comment.getUserNickname().trim().isEmpty())
                ? "匿名用户" : comment.getUserNickname();

        noticeService.createNotice(
                NoticeConstant.TYPE_COMMENT,
                title,
                actionText,
                targetType,
                targetTitle,
                targetId,
                operatorName,
                comment.getContent());
    }

    /**
     * 留言内容摘要（用于后台通知展示）
     */
    private String messageSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "留言板";
        }
        String normalized = content.trim().replaceAll("\\s+", " ");
        if (normalized.codePointCount(0, normalized.length()) <= 50) {
            return normalized;
        }
        return normalized.substring(0, normalized.offsetByCodePoints(0, 50)) + "...";
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
        return getPublicComments(CommentConstant.ZERO, id);
    }

    /**
     * 根据日常ID查询用户端可见评论
     *
     * @param id 日常ID
     * @return 评论树
     */
    @Override
    public List<CommentVo> getDailyById(Long id) {
        if (id == null || id <= 0 || dailyMapper.getDetailById(id) == null) {
            return Collections.emptyList();
        }
        return getPublicComments(CommentConstant.ONE, id);
    }

    /**
     * 查询并组装用户端评论树
     */
    private List<CommentVo> getPublicComments(Integer type, Long sourceId) {
        if (sourceId == null || sourceId <= 0) {
            return Collections.emptyList();
        }
        Comment query = Comment.builder().type(type).sourceId(sourceId).build();
        List<CommentVo> commentVoList = commentMapper.getPublicComments(query);
        if (commentVoList == null || commentVoList.isEmpty()) {
            return Collections.emptyList();
        }
        for (CommentVo comment : commentVoList) {
            comment.setAdmin(sysUserRoleMapper.hasRole(
                    comment.getUserId(), SystemConstant.SUPER_ADMIN_ROLE));
        }

        List<CommentVo> commentVos = CommentTreeUtil.buildFlatReplyTree(commentVoList);
        fillLikedStatus(commentVos);
        return commentVos;
    }

    /**
     * 为评论列表填充当前登录用户是否已点赞的状态
     *
     * @param commentVos 评论树
     */
    private void fillLikedStatus(List<CommentVo> commentVos) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || commentVos == null || commentVos.isEmpty()) {
            return;
        }
        List<CommentVo> allComments = new ArrayList<>();
        collectComments(commentVos, allComments);
        if (allComments.isEmpty()) {
            return;
        }
        List<Long> commentIds = allComments.stream()
                .map(CommentVo::getId)
                .collect(Collectors.toList());
        Set<Long> likedIds = new HashSet<>(likeMapper.selectLikedIds(
                currentUserId, LikeConstant.TARGET_COMMENT, commentIds));
        for (CommentVo vo : allComments) {
            vo.setLiked(likedIds.contains(vo.getId()));
        }
    }

    /**
     * 递归收集评论树全部节点
     */
    private void collectComments(List<CommentVo> comments, List<CommentVo> result) {
        for (CommentVo comment : comments) {
            result.add(comment);
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                collectComments(comment.getReplies(), result);
            }
        }
    }
}
