package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录风险验证信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRiskInfoVO implements Serializable {

    private String riskTicket;

    private String maskedEmail;

    private Integer expiresIn;

    private String riskReason;
}
