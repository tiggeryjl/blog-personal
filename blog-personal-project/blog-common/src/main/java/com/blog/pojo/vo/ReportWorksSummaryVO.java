package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 作品模块报表汇总
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportWorksSummaryVO implements Serializable {

    private Long articleTotal;

    private Long dailyTotal;

    private Long viewTotal;

    private Long likeTotal;

    private Long commentTotal;

    private Long categoryTotal;

    private Long tagTotal;

    private Long newArticle;

    private Long newDaily;

    private Long newView;

    private Long newComment;
}
