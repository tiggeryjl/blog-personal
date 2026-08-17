package com.blog.controller.admin;

import com.blog.pojo.dto.CategoryDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
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
    @PreAuthorize("hasPermission(null,'sys:category:add')")
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
    @PreAuthorize("hasPermission(null,'sys:category:list')")
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
    @PreAuthorize("hasPermission(null,'sys:category:list')")
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
    @PreAuthorize("hasPermission(null,'sys:category:edit')")
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
    @PreAuthorize("hasPermission(null,'sys:category:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus( @PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用分类{}的状态{}",id,status);
        categoryService.updateStatus(id,status);
        return Result.success();
    }

    /**
     * 逻辑删除分类（移入回收站）
     * @param ids
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids){
        log.info("逻辑删除分类的ids{}",ids);
        categoryService.delete(ids);
        return Result.success();
    }

    /**
     * 分页查询逻辑删除的分类（回收站）
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:list')")
    @GetMapping("/recycleList")
    public Result<PageResult> getRecycleList(RecyclePageQueryDTO param) {
        log.info("分页查询回收站分类列表:{}", param);
        return Result.success(categoryService.recyclePageQuery(param));
    }

    /**
     * 批量恢复分类（回收站 -> 正常列表）
     * @param ids 分类ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam List<Long> ids) {
        log.info("恢复回收站分类ids:{}", ids);
        categoryService.recover(ids);
        return Result.success();
    }

    /**
     * 回收站彻底删除分类
     * @param ids 分类ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:delete')")
    @DeleteMapping("/recycleDelete")
    public Result recycleDelete(@RequestParam List<Long> ids) {
        log.info("彻底删除回收站分类ids:{}", ids);
        categoryService.recycleDelete(ids);
        return Result.success();
    }

    /**
     * 获取分类下拉框数据
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:list') or hasPermission(null,'sys:article:add') or hasPermission(null,'sys:article:edit')")
    @GetMapping("/categoryOptions")
    public Result<List<OptionVO>> getCategoryOptions(){
        log.info("获取分类下拉框数据");
        List<OptionVO> optionVOList=categoryService.getCategoryOptions();
        return Result.success(optionVOList);
    }
}
