package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordEditDTO implements Serializable {

    //用户ID
    private Long userId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

    //确认新密码
    private String confirmPwd;
}

