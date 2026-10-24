package com.blog.pojo.dto;

import com.blog.validation.ValidationGroups;
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
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(groups = ValidationGroups.Update.class, message = "分类ID不能为空")
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    /**
     * 分类描述
     */
    @NotBlank(message = "分类描述不能为空")
    @Size(max = 255, message = "分类描述不能超过255个字符")
    private String description;

    /**
     * 状态 1-启用 0-禁用
     */
    @Min(value = 0, message = "分类状态不合法")
    @Max(value = 1, message = "分类状态不合法")
    private Integer status;
}
