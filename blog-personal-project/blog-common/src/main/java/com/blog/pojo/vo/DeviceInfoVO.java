package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装设备VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoVO {
    // 客户端IP
    private String ip;
    // 浏览器信息
    private String browser;
    // 操作系统
    private String os;
    // 设备类型：电脑/手机/平板
    private String deviceType;
    // 地理位置
    private String location;
}
