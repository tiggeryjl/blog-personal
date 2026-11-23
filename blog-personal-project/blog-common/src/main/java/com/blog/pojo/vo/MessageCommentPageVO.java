package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 留言板分页结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageCommentPageVO implements Serializable {

    /**
     * 主楼总数，用于分页
     */
    private long total;

    /**
     * 可见的主楼与回复总数，用于页面展示
     */
    private long commentTotal;

    private List<CommentVo> rows;
}
