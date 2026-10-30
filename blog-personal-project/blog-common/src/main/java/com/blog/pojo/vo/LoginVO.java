package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO implements Serializable {

    private String token;

    //用户视图VO
    private UserSimpleVO user;

    //是否需要登录邮箱验证码
    private Boolean needEmailCode;

    //风险登录一次性凭证
    private String riskTicket;

    //脱敏后的验证码接收邮箱
    private String maskedEmail;

    //风险凭证有效期（秒）
    private Integer expiresIn;

    //触发邮箱验证的原因
    private String riskReason;
}
