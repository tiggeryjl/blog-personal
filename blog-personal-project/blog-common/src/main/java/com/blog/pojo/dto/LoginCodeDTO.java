package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送登录邮箱验证码参数
 */
@Data
public class LoginCodeDTO implements Serializable {

    @NotBlank(message = "登录验证凭证不能为空")
    @Size(max = 64, message = "登录验证凭证格式错误")
    private String riskTicket;
}
