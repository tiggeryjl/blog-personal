package com.blog.service.impl;

import com.blog.constant.CommentConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.LikeConstant;
import com.blog.constant.StatusConstant;
import com.blog.constant.SystemConstant;
import com.blog.context.BaseContext;
import com.blog.exception.LikeException;
import com.blog.exception.UserNotLoginException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.entity.UserLike;
import com.blog.pojo.vo.LikeVo;
import com.blog.service.LikeService;
import com.blog.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 点赞服务实现
 */
@Slf4j
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private NoticeService noticeService;

    /**
     * 点赞文章/评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeVo like(Integer targetType, Long targetId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException("请先登录后再点赞");
        }
        if (targetId == null) {
            throw new LikeException("点赞目标ID不能为空");
        }
        if (LikeConstant.TARGET_ARTICLE.equals(targetType)) {
            return likeArticle(userId, targetId);
        }
        if (LikeConstant.TARGET_COMMENT.equals(targetType)) {
            return likeComment(userId, targetId);
        }
        throw new LikeException("暂不支持该类型点赞");
    }

    /**
     * 点赞文章
     */
    private LikeVo likeArticle(Long userId, Long articleId) {
        Article article = articleMapper.getArticleById(articleId);
        if (article == null || DelStatusConstant.DISABLE.equals(article.getDeleteFlag())) {
            throw new LikeException("文章不存在或已删除");
        }
        if (saveLikeRecord(userId, LikeConstant.TARGET_ARTICLE, articleId)) {
            articleMapper.changeLikeNum(articleId, 1);
            notifyArticleLike(userId, article);
        }
        Article latest = articleMapper.getArticleById(articleId);
        int likeCount = latest == null || latest.getLikeNum() == null ? 0 : latest.getLikeNum().intValue();
        return LikeVo.builder().liked(true).likeCount(likeCount).build();
    }

    /**
     * 点赞评论
     */
    private LikeVo likeComment(Long userId, Long commentId) {
        Comment comment = commentMapper.getById(commentId);
        if (comment == null || DelStatusConstant.DISABLE.equals(comment.getDeleteFlag())
                || !StatusConstant.ENABLE.equals(comment.getStatus())) {
            throw new LikeException("评论不存在或已删除");
        }
        if (saveLikeRecord(userId, LikeConstant.TARGET_COMMENT, commentId)) {
            commentMapper.changeLikeNum(commentId, 1);
            notifyCommentLike(userId, comment);
        }
        Comment latest = commentMapper.getById(commentId);
        int likeCount = latest == null || latest.getLikeNum() == null ? 0 : latest.getLikeNum();
        return LikeVo.builder().liked(true).likeCount(likeCount).build();
    }

    /**
     * 新增点赞记录，已点赞过则忽略
     *
     * @return true表示本次新增成功
     */
    private boolean saveLikeRecord(Long userId, Integer targetType, Long targetId) {
        LocalDateTime now = LocalDateTime.now();
        UserLike userLike = UserLike.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .deleteFlag(DelStatusConstant.ENABLE)
                .createTime(now)
                .updateTime(now)
                .build();
        return likeMapper.insertIgnore(userLike) > 0;
    }

    /**
     * 点赞文章时给博主推送通知(博主本人点赞不通知)
     */
    private void notifyArticleLike(Long userId, Article article) {
        if (Objects.equals(article.getUserId(), userId)
                || sysUserRoleMapper.hasRole(userId, SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }
        noticeService.createNotice("like", "收到新点赞", "点赞",
                article.getTitle(), article.getId(), getOperatorName(userId), null);
    }

    /**
     * 点赞文章评论时给博主推送通知
     */
    private void notifyCommentLike(Long userId, Comment comment) {
        if (!CommentConstant.ZERO.equals(comment.getType())
                || Objects.equals(comment.getUserId(), userId)
                || sysUserRoleMapper.hasRole(userId, SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }
        Article article = articleMapper.getArticleById(comment.getSourceId());
        if (article == null) {
            return;
        }
        noticeService.createNotice("like", "收到新点赞", "点赞评论",
                article.getTitle(), article.getId(), getOperatorName(userId), null);
    }

    /**
     * 获取点赞操作人昵称
     */
    private String getOperatorName(Long userId) {
        SysUser user = sysUserMapper.getByUserId(userId);
        return user == null || user.getNickname() == null || user.getNickname().trim().isEmpty()
                ? "匿名用户" : user.getNickname();
    }
}
