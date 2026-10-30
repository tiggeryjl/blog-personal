package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 100, message = "登录账号不能超过100个字符")
    private String loginName;

    //密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需为6-20位")
    private String password;

    //记住我
    private Boolean remember;

    // 浏览器生成并持久化的设备标识
    @Size(max = 128, message = "设备标识格式错误")
    private String deviceId;

    //邮箱验证码
    @Size(max = 20, message = "邮箱验证码格式错误")
    private String emailCode;

    //滑块验证码
    @NotBlank(message = "请完成安全验证")
    private String captchaVerifyParam;

}
