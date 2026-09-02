package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleArticleVO {

    private Long id;

    //标题
    private String title;

    //创建时间
    private LocalDateTime createTime;
}
