package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDetailVO {

    private ArticleVo article;
    private SimpleArticleVO prevArticle;
    private SimpleArticleVO nextArticle;
}
