package com.blog.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论状态修改参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentStatusDTO implements Serializable {

    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    private Long id;

    /**
     * 目标状态 0隐藏 1正常
     */
    @NotNull(message = "评论状态不能为空")
    private Integer status;
}
