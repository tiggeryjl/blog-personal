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
public class RoleDTO implements Serializable {

    @NotNull(groups = ValidationGroups.Update.class, message = "角色ID不能为空")
    private Long id;

    //角色名称
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String roleName;

    //角色标识
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 50, message = "角色标识不能超过50个字符")
    private String roleKey;

    //排序
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

    //角色描述
    @Size(max = 255, message = "角色描述不能超过255个字符")
    private String remark;

    //状态
    @Min(value = 0, message = "角色状态不合法")
    @Max(value = 1, message = "角色状态不合法")
    private Integer status;

    //逻辑删除
    private Integer deleteFlag;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;
}
