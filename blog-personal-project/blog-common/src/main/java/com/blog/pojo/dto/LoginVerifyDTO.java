package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录邮箱验证码确认参数
 */
@Data
public class LoginVerifyDTO implements Serializable {

    @NotBlank(message = "登录验证凭证不能为空")
    @Size(max = 64, message = "登录验证凭证格式错误")
    private String riskTicket;

    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码应为6位数字")
    private String emailCode;

    @NotBlank(message = "设备标识不能为空")
    @Size(max = 128, message = "设备标识格式错误")
    private String deviceId;

    private Boolean remember;
}
