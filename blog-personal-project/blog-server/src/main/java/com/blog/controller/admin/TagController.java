package com.blog.controller.admin;

import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.dto.RecyclePageQueryDTO;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 新增标签
     * @param tagDTO
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:add')")
    @PostMapping("/add")
    public Result add(@RequestBody TagDTO tagDTO){
        log.info("新增标签{}",tagDTO);
        tagService.add(tagDTO);
        return Result.success();
    }

    /**
     * 根据标签id查询
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:list')")
    @GetMapping("/{id}")
    public Result<TagVo> getById(@PathVariable Long id){
        log.info("根据标签id查询{}",id);
        TagVo tagVo=tagService.getById(id);
        return Result.success(tagVo);
    }

    /**
     * 查询所有标签信息
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:list')")
    @GetMapping()
    public Result<List<TagVo>> findAll(){
        log.info("查询所有标签信息");
        List<TagVo> list=tagService.findAll();
        return Result.success(list);
    }

    /**
     * 修改标签
     * @param tagDTO
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody TagDTO tagDTO){
        log.info("修改标签{}",tagDTO);
        tagService.update(tagDTO);
        return Result.success();
    }

    /**
     * 启用禁用标签
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus( @PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用标签{}的状态{}",id,status);
        tagService.updateStatus(id,status);
        return Result.success();
    }

    /**
     * 逻辑删除标签（移入回收站）
     * @param ids
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids){
        log.info("逻辑删除标签的ids{}",ids);
        tagService.delete(ids);
        return Result.success();
    }

    /**
     * 分页查询逻辑删除的标签（回收站）
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:list')")
    @GetMapping("/recycleList")
    public Result<PageResult> getRecycleList(RecyclePageQueryDTO param) {
        log.info("分页查询回收站标签列表:{}", param);
        return Result.success(tagService.recyclePageQuery(param));
    }

    /**
     * 批量恢复标签（回收站 -> 正常列表）
     * @param ids 标签ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam List<Long> ids) {
        log.info("恢复回收站标签ids:{}", ids);
        tagService.recover(ids);
        return Result.success();
    }

    /**
     * 回收站彻底删除标签
     * @param ids 标签ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleCategoryTag:delete')")
    @DeleteMapping("/recycleDelete")
    public Result recycleDelete(@RequestParam List<Long> ids) {
        log.info("彻底删除回收站标签ids:{}", ids);
        tagService.recycleDelete(ids);
        return Result.success();
    }

    /**
     * 获取标签下拉框数据
     * @return
     */
    @PreAuthorize("hasPermission(null,'sys:category:list') or hasPermission(null,'sys:article:add') or hasPermission(null,'sys:article:edit')")
    @GetMapping("/tagsOptions")
    public Result<List<OptionVO>> getTagOptions(){
        log.info("获取标签下拉框数据");
        List<OptionVO> optionVOList=tagService.getTagOptions();
        return Result.success(optionVOList);
    }
}
