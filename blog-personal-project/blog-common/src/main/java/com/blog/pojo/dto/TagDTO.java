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
public class TagDTO implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(groups = ValidationGroups.Update.class, message = "标签ID不能为空")
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String name;

    /**
     * 状态 1-启用 0-禁用
     */
    @Min(value = 0, message = "标签状态不合法")
    @Max(value = 1, message = "标签状态不合法")
    private Integer status;

}
