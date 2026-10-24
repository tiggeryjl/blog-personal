package com.blog.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待审核友链榜
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTopPendingLinkVO implements Serializable {

    private Long id;

    private String linkName;

    private String linkUrl;

    private String linkEmail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
