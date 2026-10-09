package com.blog.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发表留言参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageCommentDTO implements Serializable {

    /**
     * 留言类型 0评论留言 1反馈建议 2申请友链
     */
    @NotNull(message = "留言类型不能为空")
    @Min(value = 0, message = "留言类型不合法")
    @Max(value = 2, message = "留言类型不合法")
    private Integer msgType;

    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容最多500字")
    private String content;
}
