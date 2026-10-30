package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis 中保存的登录风险凭证数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRiskTicketDTO implements Serializable {

    private Long userId;

    private String email;

    private String deviceId;

    private String ip;

    private String riskReason;
}
