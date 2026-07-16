package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 状态 1-启用 0-禁用
     */
    private Integer status;

}
