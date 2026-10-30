package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录设备
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice implements Serializable {

    private Long id;

    private Long userId;

    private String deviceUuid;

    private String ipAddress;

    private String browser;

    private String os;

    private String deviceType;

    private String location;

    private String userAgent;

    private Integer isTrust;

    private LocalDateTime lastLoginTime;

    private Integer deleteFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
