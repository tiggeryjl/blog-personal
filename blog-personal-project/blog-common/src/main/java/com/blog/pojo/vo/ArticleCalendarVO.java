package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户端日历每天的文章数量
 * date 为 yyyy-MM-dd 格式的日期字符串
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCalendarVO implements Serializable {

    //日期 yyyy-MM-dd
    private String date;

    //当天发布的文章数量
    private Long count;
}
