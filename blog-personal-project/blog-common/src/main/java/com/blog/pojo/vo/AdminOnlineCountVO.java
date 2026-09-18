package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理端实时在线数消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminOnlineCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //WebSocket消息类型
    @Builder.Default
    private String messageType = "onlineCount";

    //在线管理端会话数
    private Long onlineCount;

    //在线数变更版本，用于忽略并发场景下晚到的旧消息
    private Long version;
}
