package com.blog.pojo.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "公告启用状态不能为空")
    private Boolean enabled;

    /**
     * 公告标题
     */
    @Size(max = 50, message = "公告标题不能超过50个字符")
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
