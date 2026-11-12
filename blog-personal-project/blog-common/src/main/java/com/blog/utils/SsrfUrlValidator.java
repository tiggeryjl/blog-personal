package com.blog.utils;

import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SsrfUrlValidator {

    // 允许的协议白名单
    private static final Set<String> ALLOW_SCHEME = new HashSet<>(Arrays.asList("http", "https"));
    // 内网/元数据IP段正则
    private static final Pattern INTERNAL_IP_PATTERN = Pattern.compile(
            "^(127\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.|169\\.254\\.)"
    );

    /**
     * 校验URL，不合法直接抛异常
     * @param inputUrl 外部传入的url参数
     * @return 合法URL
     * @throws Exception 非法SSRF链接
     */
    public static URL validate(String inputUrl) throws Exception {
        if (inputUrl == null || inputUrl.isBlank()) {
            throw new IllegalArgumentException("url不能为空");
        }
        URL url = new URL(inputUrl);
        // 1. 协议校验
        String scheme = url.getProtocol().toLowerCase();
        if (!ALLOW_SCHEME.contains(scheme)) {
            throw new IllegalArgumentException("不支持的协议：" + scheme);
        }
        // 2. 解析域名拿到真实IP（关键！防域名绕过）
        InetAddress address = InetAddress.getByName(url.getHost());
        String hostIp = address.getHostAddress();
        if (INTERNAL_IP_PATTERN.matcher(hostIp).find()) {
            throw new IllegalArgumentException("禁止访问内网/元数据地址");
        }
        return url;
    }
}

