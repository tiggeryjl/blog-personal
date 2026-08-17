package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 回收站分页查询参数（分类/标签等通用）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecyclePageQueryDTO implements Serializable {

    /**
     * 名称关键词
     */
    private String keyword;

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
