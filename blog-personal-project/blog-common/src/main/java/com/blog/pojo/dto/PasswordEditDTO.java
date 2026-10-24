package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 20, message = "旧密码长度需为6-20位")
    private String oldPassword;

    //新密码
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度需为6-20位")
    private String newPassword;

    //确认新密码
    @Size(min = 6, max = 20, message = "确认密码长度需为6-20位")
    private String confirmPwd;
}

