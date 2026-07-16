package com.blog.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 获取用户浏览器接系统信息工具类
 */
public class DeviceInfoUtil {

    public static UserAgent getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return UserAgent.parseUserAgentString(ua);
    }

    /**
     * 获取浏览器名称+版本
     */
    public static String getBrowserInfo(HttpServletRequest request) {
        UserAgent userAgent = getUserAgent(request);
        Browser browser = userAgent.getBrowser();
        String version = userAgent.getBrowserVersion() != null ? userAgent.getBrowserVersion().getVersion() : "";
        return browser.getName() + " " + version;
    }

    /**
     * 获取操作系统
     */
    public static String getOsInfo(HttpServletRequest request) {
        UserAgent userAgent = getUserAgent(request);
        OperatingSystem os = userAgent.getOperatingSystem();
        return os.getName();
    }

    /**
     * 判断：PC / 手机 / 平板
     */
    public static String getDeviceType(HttpServletRequest request) {
        UserAgent userAgent = getUserAgent(request);
        DeviceType deviceType = userAgent.getOperatingSystem().getDeviceType();
        switch (deviceType) {
            case COMPUTER:
                return "电脑";
            case MOBILE:
                return "手机";
            case TABLET:
                return "平板";
            default:
                return "未知设备";
        }
    }
}
