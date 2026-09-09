package com.blog.controller.user;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.vo.CategoryFrontVO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController("userCategoryController")
@RequestMapping("/user/categorys")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有启用分类信息
     * @return
     */
    @GetMapping("category")
    public Result<List<CategoryFrontVO>> findAll(){
        log.info("查询所有启用分类信息");
        List<CategoryFrontVO> list=categoryService.getCategoryList();
        return Result.success(list);
    }

}
