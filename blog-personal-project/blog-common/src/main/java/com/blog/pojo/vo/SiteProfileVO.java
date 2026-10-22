package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 站点信息（友链页展示的博客信息）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站链接，为空时用户端显示“即将上线”
     */
    private String url;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 网站简介
     */
    private String desc;
}
