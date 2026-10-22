package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 音乐分页查询参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicPageQueryDTO implements Serializable {

    /**
     * 关键字（歌名 / 歌手）
     */
    private String keyword;

    /**
     * 1上架 0下架，为空查询全部
     */
    private Integer status;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer pageSize = 10;
}
