package com.blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 音乐新增 / 编辑参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicDTO implements Serializable {

    /**
     * 编辑时必填
     */
    private Long id;

    @NotBlank(message = "歌曲名不能为空")
    @Size(max = 100, message = "歌曲名不能超过100个字符")
    private String name;

    @Size(max = 100, message = "歌手不能超过100个字符")
    private String singer;

    @NotBlank(message = "音频地址不能为空")
    @Size(max = 500, message = "音频地址不能超过500个字符")
    private String url;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String cover;

    @Size(max = 500, message = "歌词地址不能超过500个字符")
    private String lyric;

    @Min(value = 0, message = "时长不能小于0")
    private Integer duration;

    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

    /**
     * 1上架 0下架
     */
    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;
}
