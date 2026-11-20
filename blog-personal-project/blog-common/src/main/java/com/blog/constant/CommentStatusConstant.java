package com.blog.constant;

/**
 * 评论审核与展示状态
 */
public final class CommentStatusConstant {

    private CommentStatusConstant() {
    }

    // 已隐藏
    public static final Integer HIDDEN = 0;

    // 审核通过，所有用户可见
    public static final Integer APPROVED = 1;

    // 待审核，仅评论作者和管理员可见
    public static final Integer PENDING = 2;

    // 审核拒绝，不对普通用户展示
    public static final Integer REJECTED = 3;

    // 评论审核开关，存放在系统配置表
    public static final String AUDIT_ENABLED_KEY = "comment.audit.enabled";

    public static final String AUDIT_ENABLED_DESCRIPTION = "新评论是否需要管理员审核";
}
