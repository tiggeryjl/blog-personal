package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
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
}
