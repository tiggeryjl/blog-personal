package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页公告
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeNoticeVO implements Serializable {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 最后保存时间，前端用来判断是不是同一则公告
     */
    private String updatedAt;
}
