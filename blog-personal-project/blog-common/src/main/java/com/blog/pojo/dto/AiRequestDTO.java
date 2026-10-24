package com.blog.pojo.dto;

import com.blog.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiRequestDTO {

    /**
     * 主题
     */
    @NotBlank(groups = ValidationGroups.WriteArticle.class, message = "文章主题不能为空")
    @Size(max = 200, message = "文章主题不能超过200个字符")
    private String topic;

    /**
     * 风格，列如 专业/通俗/幽默
     */
    @Size(max = 50, message = "文章风格不能超过50个字符")
    private String style;

    /**
     * 文章内容长度
     */
    private String length;

    /**
     * 文本内容
     */
    @NotBlank(groups = ValidationGroups.PolishArticle.class, message = "待润色内容不能为空")
    private String content;

    /**
     * 通用对话输入内容
     */
    private String message;

    /**
     * 控制是否用pro模型
     */
    private Boolean usePro;
}
