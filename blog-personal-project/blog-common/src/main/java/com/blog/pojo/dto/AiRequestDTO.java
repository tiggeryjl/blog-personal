package com.blog.pojo.dto;

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
    private String topic;

    /**
     * 风格，列如 专业/通俗/幽默
     */
    private String style;

    /**
     * 文章内容长度
     */
    private String length;

    /**
     * 文本内容
     */
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
