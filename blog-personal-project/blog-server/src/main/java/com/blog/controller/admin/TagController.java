package com.blog.controller.admin;

import com.blog.pojo.dto.TagDTO;
import com.blog.pojo.vo.OptionVO;
import com.blog.pojo.vo.TagVo;
import com.blog.result.Result;
import com.blog.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus( @PathVariable Long id, @PathVariable Integer status){
        log.info("启用禁用标签{}的状态{}",id,status);
        tagService.updateStatus(id,status);
        return Result.success();
    }

    /**
     * 删除标签
     * @param ids
     * @return
     */
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除标签的ids{}",ids);
        tagService.delete(ids);
        return Result.success();
    }

    /**
     * 获取标签下拉框数据
     * @return
     */
    @GetMapping("/tagsOptions")
    public Result<List<OptionVO>> getTagOptions(){
        log.info("获取标签下拉框数据");
        List<OptionVO> optionVOList=tagService.getTagOptions();
        return Result.success(optionVOList);
    }
}
