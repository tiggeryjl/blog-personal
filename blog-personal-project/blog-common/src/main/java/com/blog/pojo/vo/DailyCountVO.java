package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 日常实时统计计数 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyCountVO implements Serializable {

    /**
     * 日常ID
     */
    private Long dailyId;

    /**
     * 统计数量
     */
    private Long countNum;
}
