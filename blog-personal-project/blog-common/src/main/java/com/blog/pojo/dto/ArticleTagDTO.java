package com.blog.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTagDTO implements Serializable {

    private Long id;

    //文章id
    private Long articleId;

    //标签id
    private List<Long> tagIds;

    //创建时间
    private LocalDateTime createTime;
}
