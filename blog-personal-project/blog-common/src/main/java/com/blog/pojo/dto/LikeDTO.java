package com.blog.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeDTO {

    private Long userId;

    @NotNull(message = "点赞类型不能为空")
    private Integer targetType;

    @NotNull(message = "目标ID不能为空")
    private Long targetId;
}
