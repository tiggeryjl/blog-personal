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
public class UserPageQueryDTO implements Serializable {

    //昵称
    private String nickname;

    //模糊搜索用户名，手机号，邮箱
    private String search;

    //角色
    private String role;

    //状态
    private Integer status;

    //开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime begin;

    //结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    //页码
    @Builder.Default
    private Integer page = 1;

    //每页展示的记录数
    @Builder.Default
    private Integer pageSize = 10;
}
