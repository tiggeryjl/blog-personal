package com.blog.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 音乐实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Music implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 歌曲名
     */
    private String name;

    /**
     * 歌手
     */
    private String singer;

    /**
     * 音频地址
     */
    private String url;

    /**
     * 封面地址
     */
    private String cover;

    /**
     * LRC 歌词地址
     */
    private String lyric;

    /**
     * 时长（秒）
     */
    private Integer duration;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 1上架 0下架
     */
    private Integer status;

    /**
     * 逻辑删除 0正常 1删除
     */
    private Integer deleteFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
