package com.blog.controller.admin;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.vo.CategoryVo;
import com.blog.pojo.vo.OptionVO;
import com.blog.result.Result;
import com.blog.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/categorys")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类{}",categoryDTO);
        categoryService.add(categoryDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<CategoryVo> getById(@PathVariable Long id){
        log.info("根据分类id查询{}",id);
        CategoryVo categoryVo=categoryService.getById(id);
        return Result.success(categoryVo);
    }

    /**
     * 查询所有分类信息
     * @return
     */
    @GetMapping()
    public Result<List<CategoryVo>> findAll(){
        log.info("查询所有分类信息");
        List<CategoryVo> list=categoryService.findAll();
        return Result.success(list);
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @param id
     * @return
     */
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus( @PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用分类{}的状态{}",id,status);
        categoryService.updateStatus(id,status);
        return Result.success();
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除分类的ids{}",ids);
        categoryService.delete(ids);
        return Result.success();
    }

    /**
     * 获取分类下拉框数据
     * @return
     */
    @GetMapping("/categoryOptions")
    public Result<List<OptionVO>> getCategoryOptions(){
        log.info("获取分类下拉框数据");
        List<OptionVO> optionVOList=categoryService.getCategoryOptions();
        return Result.success(optionVOList);
    }
}
