package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WebSocket通知实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 通知信息类型(点赞 or 评论)
    private String type;

    // 标题
    private String title;

    // 动作文本
    private String actionText;

    // 文章标题
    private String articleTitle;

    // 文章id
    private Long articleId;

    // 用户昵称
    private String operatorName;

    // 评论内容
    private String content;

    // 是否已读 (0=未读 1=已读)
    private Integer isRead;

    //创建时间
    private LocalDateTime createTime;
}
