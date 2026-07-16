package com.blog.pojo.dto;

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
public class UserDTO implements Serializable {

    private Long id;

    //昵称
    private String nickname;

    //用户名
    private String username;

    //手机号
    private String phone;

    //邮箱
    private String email;

    //性别 0=男 1=女 2=保密
    private Integer sex;

    //头像
    private String avatar;

    //个人简介
    private String intro;

    // 个人网站
    private String website;

    // GitHub
    private String github;

    //注册时间
    private LocalDateTime createTime;

}
