package com.blog.pojo.dto;

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
    private Long id;

    /**
     * 选中的角色id集合
     */
    private List<Long> roleIdList;

}
