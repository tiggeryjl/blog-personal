package com.blog.service;

/**
 * 邮件发送服务
 */
public interface MailService {

    /**
     * 是否已配置好发件邮箱
     */
    boolean isAvailable();

    /**
     * 发送一封 HTML 邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param html    正文
     */
    void sendHtml(String to, String subject, String html);
}
