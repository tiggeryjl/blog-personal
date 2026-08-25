package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 博客端友链申请参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkApplyDTO implements Serializable {

    /**
     * 网站名称
     */
    @NotBlank(message = "网站名称不能为空")
    private String linkName;

    /**
     * 网站链接
     */
    @NotBlank(message = "网站链接不能为空")
    private String linkUrl;

    /**
     * 站点头像图标
     */
    private String linkAvatar;

    /**
     * 站点简介
     */
    private String linkDesc;

    /**
     * 联系邮箱（预留字段，不落库）
     */
    private String email;
}
