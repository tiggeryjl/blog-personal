package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private Long id;

    //角色名称
    private String roleName;

    //角色标识
    private String roleKey;

    //排序
    private Integer sort;

    //角色描述
    private String remark;

    //状态
    private Integer status;

    //逻辑删除
    private Integer deleteFlag;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;
}
