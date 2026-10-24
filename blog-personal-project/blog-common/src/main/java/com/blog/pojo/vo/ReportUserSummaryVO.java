package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户模块报表汇总
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportUserSummaryVO implements Serializable {

    private Long userTotal;

    private Long newUser;

    private Long todayNewUser;

    private Long activeUserCount;

    private Long disabledCount;
}
