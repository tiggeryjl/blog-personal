package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 友链新增/编辑参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkDTO implements Serializable {

    /**
     * 主键ID（编辑时必填）
     */
    private Long id;

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
     * 排序权重
     */
    private Integer sort;

    /**
     * 审核状态 0待审核 1审核通过 2审核拒绝
     */
    private Integer auditStatus;

    /**
     * 展示状态 0禁用 1正常展示
     */
    private Integer status;
}
