package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 邮箱快捷注册参数（留言板简约注册）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRegisterDTO implements Serializable {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 昵称（留言板简约注册时填写，为空则取邮箱前缀）
     */
    @Size(max = 50, message = "昵称最多50字")
    private String nickname;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPwd;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "邮箱验证码应为6位数字")
    private String emailCode;

    /**
     * 注册设备标识
     */
    @NotBlank(message = "设备标识不能为空")
    @Size(max = 128, message = "设备标识格式错误")
    private String deviceId;
}
