package com.blog.controller.admin;

import com.blog.pojo.dto.OperLogPageQueryDTO;
import com.blog.pojo.vo.OperLogVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.OperLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台操作日志
 */
@Slf4j
@RestController("adminOperLogController")
@RequestMapping("/admin/operlog")
public class OperLogController {

    @Autowired
    private OperLogService operLogService;

    /**
     * 分页查询操作日志
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:operlog:list')")
    @GetMapping("/page")
    public Result<PageResult> page(OperLogPageQueryDTO param) {
        log.info("分页查询操作日志:{}", param);
        return Result.success(operLogService.pageQuery(param));
    }

    /**
     * 查询日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @PreAuthorize("hasPermission(null,'sys:operlog:list')")
    @GetMapping("/{id}")
    public Result<OperLogVO> detail(@PathVariable Long id) {
        return Result.success(operLogService.getById(id));
    }

    /**
     * 批量删除日志
     *
     * @param ids 日志ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:operlog:delete')")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除操作日志:{}", ids);
        operLogService.delete(ids);
        return Result.success();
    }

    /**
     * 清空全部日志（服务层还会硬性校验超级管理员角色）
     *
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:operlog:list')")
    @DeleteMapping("/clear")
    public Result clear() {
        log.info("清空操作日志");
        operLogService.clear();
        return Result.success();
    }
}
