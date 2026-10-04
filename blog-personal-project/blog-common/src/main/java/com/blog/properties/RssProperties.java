package com.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 新文章邮件订阅配置
 */
@Component
@ConfigurationProperties(prefix = "blog.rss")
@Data
public class RssProperties {

    //站点地址，用于拼接邮件里的文章链接和退订链接
    private String siteUrl = "http://localhost:5173";

    //站点标题，用于邮件标题和正文
    private String title = "小叶同学的博客";
}
