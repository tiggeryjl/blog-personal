package com.blog.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 阅读量文章榜
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTopArticleVO implements Serializable {

    private Long id;

    private String title;

    private String categoryName;

    private Long viewNum;

    private Long likeNum;

    private Long commentNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
