package com.blog.service.impl;

import com.blog.properties.MailProperties;
import com.blog.service.MailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 邮件发送
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MailProperties mailProperties;

    /**
     * 启动时打一条明确的日志：邮件通知到底有没有生效
     * 没配好时订阅、退订照常可用，只是不会发出任何邮件，这里先说清楚免得排查半天
     */
    @PostConstruct
    public void logMailStatus() {
        if (isAvailable()) {
            log.info("RSS新文章邮件通知已启用，发件邮箱：{}", maskEmail(mailProperties.getUsername()));
        } else {
            log.warn("RSS新文章邮件通知未启用：blog.mail.enabled={}，发件账号是否已配置={}。"
                            + "订阅与退订功能正常，但不会发出任何邮件",
                    mailProperties.getEnabled(), StringUtils.hasText(mailProperties.getUsername()));
        }
    }

    @Override
    public boolean isAvailable() {
        return Boolean.TRUE.equals(mailProperties.getEnabled())
                && StringUtils.hasText(mailProperties.getHost())
                && StringUtils.hasText(mailProperties.getUsername())
                && StringUtils.hasText(mailProperties.getPassword());
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        if (!isAvailable()) {
            log.debug("邮件未配置，跳过发送。收件人:{} 主题:{}", to, subject);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(mailProperties.getUsername(),
                    mailProperties.getFromName(), "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 打日志时把邮箱中间打码，避免完整地址进日志
     */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 3) {
            return email;
        }
        return email.substring(0, 3) + "****" + email.substring(at);
    }
}
