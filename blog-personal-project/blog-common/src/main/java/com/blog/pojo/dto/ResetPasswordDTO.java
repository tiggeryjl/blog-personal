package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 忘记密码：重置密码参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO implements Serializable {

    //邮箱验证通过后发放的一次性凭证
    @NotBlank(message = "请先完成邮箱验证")
    private String ticket;

    //新密码
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    //确认新密码
    @NotBlank(message = "确认密码不能为空")
    private String confirmPwd;
}
