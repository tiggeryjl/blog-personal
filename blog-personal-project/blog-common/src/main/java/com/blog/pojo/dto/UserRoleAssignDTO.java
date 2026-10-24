package com.blog.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleAssignDTO implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /**
     * 选中的角色id集合
     */
    @NotNull(message = "角色集合不能为空")
    private List<Long> roleIdList;

}
