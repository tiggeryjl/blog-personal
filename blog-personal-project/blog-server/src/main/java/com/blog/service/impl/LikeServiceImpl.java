package com.blog.service.impl;

import com.blog.constant.CommentConstant;
import com.blog.constant.DailyStatusConstant;
import com.blog.constant.DelStatusConstant;
import com.blog.constant.LikeConstant;
import com.blog.constant.NoticeConstant;
import com.blog.constant.StatusConstant;
import com.blog.constant.SystemConstant;
import com.blog.context.BaseContext;
import com.blog.exception.LikeException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.DailyMapper;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.SysUserMapper;
import com.blog.mapper.SysUserRoleMapper;
import com.blog.pojo.entity.Article;
import com.blog.pojo.entity.Comment;
import com.blog.pojo.entity.Daily;
import com.blog.pojo.entity.SysUser;
import com.blog.pojo.entity.UserLike;
import com.blog.pojo.vo.ArticleCountVO;
import com.blog.pojo.vo.LikeVo;
import com.blog.service.LikeService;
import com.blog.service.NoticeService;
import com.blog.utils.NoticeTextUtil;
import com.blog.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    private DailyMapper dailyMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private NoticeService noticeService;

    /**
     * 点赞文章、日常或评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeVo like(Integer targetType, Long targetId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            // 未登录也允许点赞
            userId = resolveGuestUserId();
        }
        if (targetId == null) {
            throw new LikeException("点赞目标ID不能为空");
        }
        if (LikeConstant.TARGET_ARTICLE.equals(targetType)) {
            return likeArticle(userId, targetId);
        }
        if (LikeConstant.TARGET_DAILY.equals(targetType)) {
            return likeDaily(userId, targetId);
        }
        if (LikeConstant.TARGET_COMMENT.equals(targetType)) {
            return likeComment(userId, targetId);
        }
        throw new LikeException("暂不支持该类型点赞");
    }

    /**
     * 为未登录用户生成游客身份ID（使用负数，避免与真实用户自增ID冲突）
     *
     * @return 游客身份ID
     */
    private Long resolveGuestUserId() {
        String ip = currentClientIp();
        if (ip == null || ip.trim().isEmpty()) {
            ip = "unknown";
        }
        String key = ip.trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (bytes[i] & 0xff);
            }
            return -(Math.abs(value % 1_000_000_000L)) - 1L;
        } catch (NoSuchAlgorithmException e) {
            return -(Math.abs((long) key.hashCode()) % 1_000_000_000L) - 1L;
        }
    }

    /**
     * 获取当前请求的客户端IP
     */
    private String currentClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            return IpUtil.getClientIp(attributes.getRequest());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 点赞公开日常
     */
    private LikeVo likeDaily(Long userId, Long dailyId) {
        Daily daily = getPublicDaily(dailyId);
        if (saveLikeRecord(userId, LikeConstant.TARGET_DAILY, dailyId)) {
            if (dailyMapper.changeLikeNum(dailyId, 1) == 0) {
                throw new LikeException("日常不存在或暂未公开");
            }
            notifyDailyLike(userId, daily);
        }
        List<ArticleCountVO> counts = likeMapper.countByTargetIds(
                LikeConstant.TARGET_DAILY, Collections.singletonList(dailyId));
        long likeCount = counts.isEmpty() || counts.get(0).getCountNum() == null
                ? 0L : counts.get(0).getCountNum();
        return LikeVo.builder().liked(true).likeCount((int) likeCount).build();
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
        List<ArticleCountVO> counts = likeMapper.countByTargetIds(
                LikeConstant.TARGET_ARTICLE, Collections.singletonList(articleId));
        long likeCount = counts.isEmpty() || counts.get(0).getCountNum() == null
                ? 0L : counts.get(0).getCountNum();
        return LikeVo.builder().liked(true).likeCount((int) likeCount).build();
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
        if (CommentConstant.ONE.equals(comment.getType())) {
            validateVisibleReplyChain(comment);
            getPublicDaily(comment.getSourceId());
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
        noticeService.createNotice(NoticeConstant.TYPE_LIKE, "收到新点赞", "点赞",
                NoticeConstant.TARGET_ARTICLE, article.getTitle(), article.getId(),
                getOperatorName(userId), null);
    }

    /**
     * 点赞日常时给后台推送通知
     */
    private void notifyDailyLike(Long userId, Daily daily) {
        if (Objects.equals(daily.getUserId(), userId)
                || sysUserRoleMapper.hasRole(userId, SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }
        noticeService.createNotice(NoticeConstant.TYPE_LIKE, "收到新点赞", "点赞",
                NoticeConstant.TARGET_DAILY,
                NoticeTextUtil.dailySummary(daily.getContent(), daily.getId()),
                daily.getId(), getOperatorName(userId), null);
    }

    /**
     * 点赞文章或日常评论时给后台推送通知
     *
     * 通知面向后台通知中心，评论作者点赞自己的评论同样推送，
     * 仅博主本人（超级管理员）操作不通知
     */
    private void notifyCommentLike(Long userId, Comment comment) {
        if (sysUserRoleMapper.hasRole(userId, SystemConstant.SUPER_ADMIN_ROLE)) {
            return;
        }

        if (CommentConstant.ZERO.equals(comment.getType())) {
            Article article = articleMapper.getArticleById(comment.getSourceId());
            if (article == null) {
                return;
            }
            noticeService.createNotice(NoticeConstant.TYPE_LIKE, "收到新点赞", "点赞评论",
                    NoticeConstant.TARGET_ARTICLE, article.getTitle(), article.getId(),
                    getOperatorName(userId), null);
            return;
        }

        if (CommentConstant.ONE.equals(comment.getType())) {
            Daily daily = getPublicDaily(comment.getSourceId());
            noticeService.createNotice(NoticeConstant.TYPE_LIKE, "收到新点赞", "点赞评论",
                    NoticeConstant.TARGET_DAILY,
                    NoticeTextUtil.dailySummary(daily.getContent(), daily.getId()),
                    daily.getId(), getOperatorName(userId), null);
        }
    }

    private Daily getPublicDaily(Long dailyId) {
        Daily daily = dailyMapper.getById(dailyId);
        if (daily == null || DelStatusConstant.DISABLE.equals(daily.getDeleteFlag())
                || !DailyStatusConstant.PUBLISHED.equals(daily.getStatus())) {
            throw new LikeException("日常不存在或暂未公开");
        }
        return daily;
    }

    /**
     * 点赞回复时确保从当前评论到顶级评论的整条链路均可见且来源一致
     */
    private void validateVisibleReplyChain(Comment target) {
        Integer type = target.getType();
        Long sourceId = target.getSourceId();
        Comment current = target;
        Set<Long> visitedIds = new HashSet<>();

        while (current != null) {
            if (current.getId() == null || !visitedIds.add(current.getId())) {
                throw new LikeException("评论回复关系异常");
            }
            if (DelStatusConstant.DISABLE.equals(current.getDeleteFlag())
                    || !StatusConstant.ENABLE.equals(current.getStatus())) {
                throw new LikeException("评论不存在或已隐藏");
            }
            if (!Objects.equals(type, current.getType())
                    || !Objects.equals(sourceId, current.getSourceId())) {
                throw new LikeException("评论回复关系异常");
            }
            if (current.getParentId() == null || current.getParentId() == 0L) {
                return;
            }
            current = commentMapper.getById(current.getParentId());
            if (current == null) {
                throw new LikeException("评论回复关系异常");
            }
        }
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
