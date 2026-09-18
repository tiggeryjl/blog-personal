package com.blog.controller.user;

import com.blog.pojo.vo.ArticleFrontVO;
import com.blog.result.Result;
import com.blog.service.HomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端首页接口
 */
@Slf4j
@RestController("userHomeController")
@RequestMapping("/user/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取首页热门文章，最多返回20条
     *
     * @return 首页热门文章列表
     */
    @GetMapping("/popular")
    public Result<List<ArticleFrontVO>> getPopularArticles() {
        log.info("获取用户端首页热门文章");
        List<ArticleFrontVO> articleFrontVOList = homeService.getPopularArticles();
        return Result.success(articleFrontVOList);
    }
}
