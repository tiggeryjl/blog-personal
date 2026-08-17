package com.blog.controller.admin;

import com.blog.pojo.dto.DailyPageQueryDTO;
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
