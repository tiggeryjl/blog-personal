package com.blog.controller.admin;

import com.blog.pojo.dto.LinkAuditDTO;
import com.blog.pojo.dto.LinkDTO;
import com.blog.pojo.dto.LinkPageQueryDTO;
import com.blog.pojo.vo.LinkVo;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.LinkService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 友链管理
 */
@Slf4j
@RestController("adminLinkController")
@RequestMapping("/admin/link")
public class LinkController {

    @Autowired
    private LinkService linkService;

    /**
     * 分页查询友链列表（全部/待审核/已通过/已拒绝）
     * @param param 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:list')")
    @GetMapping("/page")
    public Result<PageResult> page(LinkPageQueryDTO param) {
        log.info("分页查询友链列表:{}", param);
        return Result.success(linkService.pageQuery(param));
    }

    /**
     * 统计各审核状态数量
     *
     * @return total/pending/approved/rejected
     */
    @PreAuthorize("hasPermission(null,'sys:link:list')")
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        log.info("统计友链审核状态数量");
        return Result.success(linkService.stats());
    }

    /**
     * 根据ID查询友链
     *
     * @param id 友链ID
     * @return 友链信息
     */
    @PreAuthorize("hasPermission(null,'sys:link:list')")
    @GetMapping("/{id}")
    public Result<LinkVo> getById(@PathVariable Long id) {
        log.info("根据ID查询友链:{}", id);
        return Result.success(linkService.getById(id));
    }

    /**
     * 新增友链
     *
     * @param linkDTO 友链参数
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:add')")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody LinkDTO linkDTO) {
        log.info("新增友链:{}", linkDTO);
        linkService.add(linkDTO);
        return Result.success();
    }

    /**
     * 编辑友链
     *
     * @param linkDTO 友链参数
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:edit')")
    @PutMapping("/update")
    public Result update(@Valid @RequestBody LinkDTO linkDTO) {
        log.info("编辑友链:{}", linkDTO);
        linkService.update(linkDTO);
        return Result.success();
    }

    /**
     * 审核友链（通过/拒绝）
     *
     * @param linkAuditDTO 审核参数
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:edit')")
    @PutMapping("/audit")
    public Result audit(@Valid @RequestBody LinkAuditDTO linkAuditDTO) {
        log.info("审核友链:{}", linkAuditDTO);
        linkService.audit(linkAuditDTO);
        return Result.success();
    }

    /**
     * 启用/禁用友链
     *
     * @param id     友链ID
     * @param status 目标展示状态
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:edit')")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("启用/禁用友链:{} status:{}", id, status);
        linkService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 批量逻辑删除友链
     *
     * @param ids 友链ID集合
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:link:delete')")
    @DeleteMapping()
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除友链ids:{}", ids);
        linkService.delete(ids);
        return Result.success();
    }
}
