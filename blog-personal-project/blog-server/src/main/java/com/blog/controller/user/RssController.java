package com.blog.controller.user;

import com.blog.pojo.dto.RssSubscribeDTO;
import com.blog.result.Result;
import com.blog.service.RssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 RSS 订阅（订阅源 + 邮箱订阅）
 */
@Slf4j
@RestController("userRssController")
@RequestMapping("/user/rss")
public class RssController {

    @Autowired
    private RssService rssService;

    /**
     * 订阅新文章通知
     */
    @PostMapping("/subscribe")
    public Result subscribe(@RequestBody RssSubscribeDTO dto) {
        log.info("RSS邮箱订阅：{}", dto.getEmail());
        rssService.subscribe(dto);
        return Result.success();
    }

    /**
     * 通过邮件里的退订链接取消订阅
     */
    @GetMapping("/unsubscribe")
    public Result<String> unsubscribe(@RequestParam("token") String token) {
        boolean success = rssService.unsubscribe(token);
        return Result.success(success ? "退订成功，以后不会再收到新文章邮件" : "退订链接无效或已经退订过了");
    }

    /**
     * 当前订阅人数，前端用于展示
     */
    @GetMapping("/count")
    public Result<Long> count() {
        Long count = rssService.countActive();
        return Result.success(count);
    }
}
