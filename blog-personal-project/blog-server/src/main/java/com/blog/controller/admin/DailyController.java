package com.blog.controller.admin;

import com.blog.pojo.dto.DailyDTO;
import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.entity.Daily;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.DailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日常回收站管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/daily")
public class DailyController {

    @Autowired
    private DailyService dailyService;

    /**
     * 分页查询日常列表
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:list')")
    @GetMapping("/list")
    public Result<PageResult> getDailyList(DailyPageQueryDTO param) {
        log.info("分页查询日常列表:{}", param);
        return Result.success(dailyService.pageQuery(param));
    }

    /**
     * 新增日常
     * @param dailyDTO 日常信息
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:add')")
    @PostMapping("/add")
    public Result add(@RequestBody DailyDTO dailyDTO) {
        log.info("新增日常:{}", dailyDTO);
        dailyService.add(dailyDTO);
        return Result.success();
    }

    /**
     * 根据ID查询日常
     * @param id 日常ID
     * @return 日常信息
     */
    @PreAuthorize("hasPermission(null,'sys:works:list')")
    @GetMapping("/{id}")
    public Result<Daily> getById(@PathVariable Long id) {
        log.info("查询id为{}的日常", id);
        return Result.success(dailyService.getById(id));
    }

    /**
     * 修改日常
     * @param dailyDTO 日常信息
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:edit')")
    @PutMapping("/update")
    public Result update(@RequestBody DailyDTO dailyDTO) {
        log.info("修改日常:{}", dailyDTO);
        dailyService.update(dailyDTO);
        return Result.success();
    }

    /**
     * 修改日常状态（立即发布/下架/上架/私密等）
     * @param dailyDTO 日常ID与目标状态
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:edit')")
    @PutMapping("/status")
    public Result updateStatus(@RequestBody DailyDTO dailyDTO) {
        log.info("修改日常状态:{}", dailyDTO);
        dailyService.updateStatus(dailyDTO);
        return Result.success();
    }

    /**
     * 置顶切换
     * @param id 日常ID
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:edit')")
    @PutMapping("/{id}")
    public Result updateTop(@PathVariable Long id) {
        log.info("设置置顶日常id:{}", id);
        dailyService.updateTop(id);
        return Result.success();
    }

    /**
     * 设置定时发布
     * @param dailyDTO 日常ID与定时时间
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:edit')")
    @PostMapping("/setTimed")
    public Result setTimed(@RequestBody DailyDTO dailyDTO) {
        log.info("设置日常定时发布:{}", dailyDTO);
        dailyService.setTimedPublish(dailyDTO);
        return Result.success();
    }

    /**
     * 取消定时发布
     * @param id 日常ID
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:edit')")
    @PostMapping("/cancelTimed/{id}")
    public Result cancelTimed(@PathVariable Long id) {
        log.info("取消日常定时发布id:{}", id);
        dailyService.cancelTimedPublish(id);
        return Result.success();
    }

    /**
     * 批量逻辑删除日常（移入回收站）
     * @param ids 日常ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:works:delete')")
    @DeleteMapping("/logicDelete")
    public Result logicDelete(@RequestParam List<Long> ids) {
        log.info("逻辑删除日常ids:{}", ids);
        dailyService.logicDelete(ids);
        return Result.success();
    }

    /**
     * 分页查询逻辑删除的日常（回收站）
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleDailyWork:list')")
    @GetMapping("/recycleList")
    public Result<PageResult> getRecycleList(DailyPageQueryDTO param) {
        log.info("分页查询回收站日常列表:{}", param);
        return Result.success(dailyService.recyclePageQuery(param));
    }

    /**
     * 批量恢复日常（回收站 -> 正常列表）
     * @param ids 日常ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleDailyWork:recycle')")
    @PutMapping("/recover")
    public Result recover(@RequestParam List<Long> ids) {
        log.info("恢复回收站日常ids:{}", ids);
        dailyService.recover(ids);
        return Result.success();
    }

    /**
     * 回收站批量彻底删除日常
     * @param ids 日常ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:recycleDailyWork:delete')")
    @DeleteMapping("/recycleDelete")
    public Result recycleDelete(@RequestParam List<Long> ids) {
        log.info("彻底删除回收站日常ids:{}", ids);
        dailyService.delete(ids);
        return Result.success();
    }
}
