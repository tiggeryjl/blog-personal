package com.blog.controller.admin;

import com.blog.pojo.dto.MusicDTO;
import com.blog.pojo.dto.MusicPageQueryDTO;
import com.blog.pojo.vo.MusicVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.MusicService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 音乐管理
 */
@Slf4j
@RestController("adminMusicController")
@RequestMapping("/admin/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 分页查询音乐
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:music:list')")
    @GetMapping("/page")
    public Result<PageResult> page(MusicPageQueryDTO param) {
        return Result.success(musicService.pageQuery(param));
    }

    /**
     * 根据ID查询音乐
     *
     * @param id 音乐ID
     * @return 音乐信息
     */
    @PreAuthorize("hasPermission(null,'sys:music:list')")
    @GetMapping("/{id}")
    public Result<MusicVO> getById(@PathVariable Long id) {
        return Result.success(musicService.getById(id));
    }

    /**
     * 新增音乐
     *
     * @param musicDTO 音乐参数
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:music:add')")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody MusicDTO musicDTO) {
        log.info("新增音乐:{}", musicDTO.getName());
        musicService.add(musicDTO);
        return Result.success();
    }

    /**
     * 编辑音乐
     *
     * @param musicDTO 音乐参数
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:music:edit')")
    @PutMapping("/update")
    public Result update(@Valid @RequestBody MusicDTO musicDTO) {
        log.info("编辑音乐:{}", musicDTO.getId());
        musicService.update(musicDTO);
        return Result.success();
    }

    /**
     * 上下架
     *
     * @param id     音乐ID
     * @param status 1上架 0下架
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:music:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        musicService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 批量删除
     *
     * @param ids 音乐ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:music:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除音乐:{}", ids);
        musicService.delete(ids);
        return Result.success();
    }
}
