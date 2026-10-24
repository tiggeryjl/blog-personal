package com.blog.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleMenuAssignDTO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "菜单权限不能为空")
    private List<Long> menuIdList;
}
