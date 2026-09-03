package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 后台回复评论参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentReplyDTO implements Serializable {

    /**
     * 被回复的评论ID
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容最多500字")
    private String content;
}
