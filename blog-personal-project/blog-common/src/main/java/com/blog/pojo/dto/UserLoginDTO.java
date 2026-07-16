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
public class UserLoginDTO implements Serializable {

    //昵称
    private String nickname;

    //登录用户名
    private String loginName;

    //密码
    private String password;

    //记住密码
    private String remember;

    //邮箱验证码
    private String emailCode;

    //滑块验证码
    private String captchaVerifyParam;

}
