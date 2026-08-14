package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页趋势数据单项 -- 某个时间点的某个实体增量
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeTrendItemVO implements Serializable {

    //时间点
    private String period;

    //数量
    private Long count;
}
