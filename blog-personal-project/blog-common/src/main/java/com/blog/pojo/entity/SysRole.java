package com.blog.pojo.entity;

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
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

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
