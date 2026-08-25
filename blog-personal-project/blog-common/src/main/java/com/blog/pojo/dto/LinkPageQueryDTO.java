package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 友链分页查询参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkPageQueryDTO implements Serializable {

    /**
     * 关键字（网站名称/链接模糊匹配）
     */
    private String keyword;

    /**
     * 审核状态 0待审核 1审核通过 2审核拒绝，为空查询全部
     */
    private Integer auditStatus;

    /**
     * 展示状态 0禁用 1正常展示，为空查询全部
     */
    private Integer status;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页显示的记录数
     */
    @Builder.Default
    private Integer pageSize = 10;
}
