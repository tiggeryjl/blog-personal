package com.blog.pojo.vo;

import com.blog.pojo.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论展示对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentVo implements Serializable {

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
     * 来源标题(文章标题/日常内容摘要/留言板)
     */
    private String sourceTitle;

    /**
     * 留言类型 0评论留言 1反馈建议 2申请友链
     */
    private Integer msgType;

    /**
     * 父评论ID 0=顶级评论
     */
    private Long parentId;

    /**
     * 父评论人昵称
     */
    private String parentNickname;

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
     * 回复数(该评论下的二级回复数量)
     */
    private Long replyCount;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 子回复集合
     */
    private List<CommentVo> replies = new ArrayList<>();

    /**
     * 是否为博主
     */
    private boolean admin;

    /**
     * 当前登录用户是否已点赞
     */
    private boolean liked;
}
