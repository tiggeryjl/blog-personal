package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolePageQueryDTO implements Serializable {

    //角色名称
    private String roleName;

    //状态
    private Integer status;

    //页码
    @Builder.Default
    private Integer page = 1;

    //每页展示的记录数
    @Builder.Default
    private Integer pageSize = 10;
}
