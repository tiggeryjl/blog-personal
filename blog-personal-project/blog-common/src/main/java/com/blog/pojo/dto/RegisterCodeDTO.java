package com.blog.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送注册邮箱验证码参数
 */
@Data
public class RegisterCodeDTO implements Serializable {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;
}
