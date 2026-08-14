package com.blog.controller.admin;

import com.blog.pojo.dto.SysNoticeDTO;
import com.blog.pojo.vo.InitNoticeVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket通知
 */
@Slf4j
@RestController
@RequestMapping("/admin/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     *  统计未读数和最新5条通知
     * @return
     */
    @GetMapping("/initUnread")
    public Result<InitNoticeVO> initUnread() {
        log.info("统计未读数和最新5条通知");
        return Result.success(noticeService.getInitUnread());
    }

    /**
     * 分页查询通知信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询通知信息");
        return Result.success(noticeService.pageNotice(pageNum, pageSize));
    }

    /**
     * 标记单条已读
     * @param id
     * @return
     */
    @PutMapping("/read/{id}")
    public Result readSingle(@PathVariable Long id) {
        log.info("标记单条已读id{}:",id);
        noticeService.markReadSingle(id);
        return Result.success();
    }

    /**
     * 一键全部标记已读
     * @return
     */
    @PutMapping("/readAll")
    public Result readAll() {
        log.info("一键全部标记已读");
        noticeService.markReadAll();
        return Result.success();
    }
}
