package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeDTO implements Serializable {

    private Long id;

    // 通知信息类型(点赞 or 评论)
    private String type;

    // 标题
    private String title;

    // 动作文本
    private String actionText;

    // 跳转目标类型 article/daily/link
    private String targetType;

    // 跳转目标标题或摘要
    private String targetTitle;

    // 跳转目标ID
    private Long targetId;

    // 用户昵称
    private String operatorName;

    // 评论内容
    private String content;

    // 是否已读 (0=未读 1=已读)
    private Integer isRead;
    
    //创建时间
    private String createTime;
}
