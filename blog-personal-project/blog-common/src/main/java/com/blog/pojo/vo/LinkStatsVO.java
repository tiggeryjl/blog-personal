package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 友链审核状态统计
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkStatsVO implements Serializable {

    private Long total;

    private Long pending;

    private Long approved;

    private Long rejected;
}
