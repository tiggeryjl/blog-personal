package com.blog.controller.user;

import com.blog.pojo.dto.DailyPageQueryDTO;
import com.blog.pojo.vo.DailyFrontVO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.service.DailyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端日常接口
 */
@Slf4j
@RestController("userDailyController")
@RequestMapping("/user/daily")
public class DailyController {

    @Autowired
    private DailyService dailyService;

    /**
     * 分页查询用户端日常列表
     *
     * @param param 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult> getDailyList(@Valid DailyPageQueryDTO param) {
        log.info("用户端分页查询日常列表:{}", param);
        return Result.success(dailyService.pageQueryUser(param));
    }

    /**
     * 根据ID查询用户端日常详情
     *
     * @param id 日常ID
     * @return 日常详情
    */
    @GetMapping("/getDailyDetail/{id}")
    public Result<DailyFrontVO> getDailyDetail(@PathVariable("id") Long id) {
        log.info("用户端查询日常详情,id:{}", id);
        DailyFrontVO daily = dailyService.getDetailById(id);
        if (daily == null) {
            return Result.error("日常不存在或暂未公开");
        }
        return Result.success(daily);
    }

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
