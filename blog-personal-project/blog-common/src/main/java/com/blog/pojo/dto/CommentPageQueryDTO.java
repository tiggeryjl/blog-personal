package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论分页查询参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentPageQueryDTO implements Serializable {

    /**
     * 评论类型 0文章评论 1日常评论 2留言板
     */
    private Integer type;

    /**
     * 来源ID 文章ID/日常ID
     */
    private Long sourceId;

    /**
     * 关键字(评论内容/评论人昵称/回复人昵称)
     */
    private String keyword;

    /**
     * 留言类型 0评论留言 1反馈建议 2申请友链
     */
    private Integer msgType;

    /**
     * 状态 0隐藏 1正常
     */
    private Integer status;

    /**
     * 是否置顶 0否 1是
     */
    private Integer isTop;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页显示的记录数
     */
    @Builder.Default
    private Integer pageSize = 10;
}
