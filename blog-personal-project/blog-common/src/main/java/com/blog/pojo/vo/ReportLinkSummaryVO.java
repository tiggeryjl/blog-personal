package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 友链模块报表汇总
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportLinkSummaryVO implements Serializable {

    private Long linkTotal;

    private Long pendingCount;

    private Long approvedCount;

    private Long rejectedCount;

    private Long newApply;

    private Long displayCount;
}
