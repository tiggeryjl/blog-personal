package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 周报阅读量文章榜
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTopWeeklyArticleVO implements Serializable {

    private Long id;

    private String title;

    private Long viewCount;
}
