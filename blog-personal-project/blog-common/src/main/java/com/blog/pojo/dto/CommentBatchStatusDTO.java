package com.blog.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 评论批量审核参数
 */
@Data
public class CommentBatchStatusDTO implements Serializable {

    @NotEmpty(message = "评论ID不能为空")
    private List<Long> ids;

    @NotNull(message = "评论状态不能为空")
    private Integer status;
}
