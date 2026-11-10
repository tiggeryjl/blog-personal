package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 网站备案信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SiteFilingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ICP备案号
     */
    private String icp;

    /**
     * 公安备案号
     */
    private String police;
}
