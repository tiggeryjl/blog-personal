package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报表分布数据项（饼图/环形图）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDistributionVO implements Serializable {

    /**
     * 分布名称
     */
    private String name;

    /**
     * 数量
     */
    private Long value;
}
