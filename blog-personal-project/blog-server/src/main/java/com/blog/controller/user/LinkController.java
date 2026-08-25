package com.blog.controller.user;

import com.blog.pojo.dto.LinkApplyDTO;
import com.blog.pojo.vo.LinkVo;
import com.blog.result.Result;
import com.blog.service.LinkService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 博客端友链接口（申请/展示）
 */
@Slf4j
@RestController("userLinkController")
@RequestMapping("/link")
public class LinkController {

    @Autowired
    private LinkService linkService;

    /**
     * 提交友链申请（进入待审核）
     *
     * @param linkApplyDTO 申请参数
     * @return 统一结果
     */
    @PostMapping("/apply")
    public Result apply(@Valid @RequestBody LinkApplyDTO linkApplyDTO) {
        log.info("提交友链申请:{}", linkApplyDTO.getLinkName());
        linkService.apply(linkApplyDTO);
        return Result.success();
    }

    /**
     * 查询已通过且启用的友链列表
     *
     * @return 友链列表
     */
    @GetMapping("/list")
    public Result<List<LinkVo>> list() {
        log.info("查询已通过的友链列表");
        return Result.success(linkService.listPublic());
    }

    /**
     * 查询全部待审核/已拒绝的友链申请（按申请时间倒序）
     *
     * @return 友链列表
     */
    @GetMapping("/applications")
    public Result<List<LinkVo>> applications() {
        log.info("查询全部待审核/已拒绝的友链申请");
        return Result.success(linkService.listApplications());
    }

    /**
     * 催促审核本人的友链申请（需登录）
     *
     * @param id 友链申请ID
     * @return 统一结果
     */
    @PostMapping("/urge/{id}")
    public Result urge(@PathVariable Long id) {
        log.info("催促审核友链申请:{}", id);
        linkService.urge(id);
        return Result.success();
    }
}
