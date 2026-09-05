package com.blog.controller.user;

import com.blog.result.Result;
import com.blog.service.DailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端日常接口
 */
@Slf4j
@RestController
@RequestMapping("/user/daily")
public class UserDailyController {

    @Autowired
    private DailyService dailyService;

    /**
     * 日常浏览数 +1
     *
     * @param id 日常ID
     */
    @PutMapping("/{id}/view")
    public Result addView(@PathVariable("id") Long id) {
        log.info("日常浏览+1:{}", id);
        dailyService.incrementView(id);
        return Result.success();
    }
}
