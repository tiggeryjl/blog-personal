package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章浏览记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleViewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long articleId;

    private Long userId;

    private String ipAddress;

    private String browser;

    private String os;

    private String deviceType;

    private String userAgent;
}
