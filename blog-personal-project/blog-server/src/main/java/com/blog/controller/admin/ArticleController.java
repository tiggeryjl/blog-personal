package com.blog.controller.admin;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/article")
public class ArticleController {

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody CategoryDTO categoryDTO){
        return Result.success();
    }

}
