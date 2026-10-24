package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 活跃用户榜
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTopActiveUserVO implements Serializable {

    private Long id;

    private String nickname;

    private String avatar;

    private Long articleCount;

    private Long commentCount;

    private Long activityCount;
}
