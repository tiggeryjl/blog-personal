package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RSS 邮箱订阅记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssSubscription implements Serializable {

    //主键
    private Long id;

    //访客ID，未登录订阅时为 null
    private Long visitorId;

    //昵称，未填写时用邮箱前缀
    private String nickname;

    //订阅邮箱
    private String email;

    //退订令牌，邮件退订链接使用
    private String token;

    //是否订阅中 0-已退订 1-订阅中
    private Integer isActive;

    //订阅时间
    private LocalDateTime subscribeTime;

    //取消订阅时间
    private LocalDateTime unSubscribeTime;
}
