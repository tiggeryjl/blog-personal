package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论模块报表汇总
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCommentSummaryVO implements Serializable {

    private Long commentTotal;

    private Long newComment;

    private Long normalCount;

    private Long hiddenCount;

    private Long articleCommentCount;

    private Long dailyCommentCount;

    private Long messageCommentCount;
}
