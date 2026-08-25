package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 友情链接实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 网站名称
     */
    private String linkName;

    /**
     * 网站链接
     */
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
     * 申请邮箱
     */
    private String linkEmail;

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

    /**
     * 逻辑删除 0=正常 1=删除
     */
    private Integer deleteFlag;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
