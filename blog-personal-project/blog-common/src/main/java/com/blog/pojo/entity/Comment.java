package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公共评论实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 评论类型 0文章评论 1日常评论 2留言板
     */
    private Integer type;

    /**
     * 来源ID 文章ID/日常ID/留言板来源ID
     */
    private Long sourceId;

    /**
     * 留言类型 0评论留言 1反馈建议 2申请友链
     */
    private Integer msgType;

    /**
     * 父评论ID 0=顶级评论
     */
    private Long parentId;

    /**
     * 回复用户ID 0=没有
     */
    private Long replyUserId;

    /**
     * 回复的用户昵称
     */
    private String replyUserNickname;

    /**
     * 评论人ID
     */
    private Long userId;

    /**
     * 评论人昵称
     */
    private String userNickname;

    /**
     * 评论人头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeNum;

    /**
     * 状态 0隐藏 1正常
     */
    private Integer status;

    /**
     * 是否置顶 0否 1是
     */
    private Integer isTop;

    /**
     * 评论时IP地址
     */
    private String ipAddress;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 操作地理位置
     */
    private String location;

    /**
     * 原始UA头(浏览器User-Agent)
     */
    private String userAgent;

    /**
     * 逻辑删除 0=正常 1=删除
     */
    private Integer deleteFlag;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
