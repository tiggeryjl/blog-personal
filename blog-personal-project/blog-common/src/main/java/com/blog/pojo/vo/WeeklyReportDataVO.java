package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 周报基础数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyReportDataVO implements Serializable {

    private Long views;

    private Long comments;

    private Long newArticles;

    private Long newUsers;
}
