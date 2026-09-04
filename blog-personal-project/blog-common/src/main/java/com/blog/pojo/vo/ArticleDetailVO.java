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

    /**
     * 文章详情
     */
    private ArticleVo articleVo;

    /**
     * 上一页
     */
    private SimpleArticleVO prevArticle;

    /**
     * 下一页
     */
    private SimpleArticleVO nextArticle;

    /**
     * 当前登录用户是否已点赞文章
     */
    private boolean liked;
}
