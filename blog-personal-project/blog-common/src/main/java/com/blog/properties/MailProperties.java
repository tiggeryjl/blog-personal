package com.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件发送配置（用于新文章通知订阅者）
 */
@Component
@ConfigurationProperties(prefix = "blog.mail")
@Data
public class MailProperties {

    //总开关，未配置邮箱账号时保持 false，订阅功能照常可用，只是不发信
    private Boolean enabled = false;

    //SMTP 服务器，例如 smtp.qq.com
    private String host;

    //SMTP 端口
    private Integer port = 465;

    //是否使用 SSL
    private Boolean ssl = true;

    //发件邮箱账号
    private String username;

    //发件邮箱授权码
    private String password;

    //发件人展示名
    private String fromName = "小叶同学的博客";
}
