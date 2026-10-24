package com.blog.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞日常榜
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTopDailyVO implements Serializable {

    private Long id;

    private String content;

    private Long likeNum;

    private Long commentNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
