package com.blog.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 音乐视图对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String singer;

    private String url;

    private String cover;

    private String lyric;

    private Integer duration;

    private Integer sort;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
