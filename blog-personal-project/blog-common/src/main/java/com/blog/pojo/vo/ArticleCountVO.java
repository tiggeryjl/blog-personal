package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章实时统计计数 VO：按文章 ID 聚合点赞数/评论数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCountVO implements Serializable {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 统计数量
     */
    private Long countNum;
}
