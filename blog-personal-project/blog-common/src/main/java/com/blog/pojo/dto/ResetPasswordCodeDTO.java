package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 忘记密码：发送邮箱验证码参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordCodeDTO implements Serializable {

    //注册邮箱
    @NotBlank(message = "邮箱不能为空")
    private String email;
}
