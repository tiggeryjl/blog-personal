package com.blog.controller.user;

import com.blog.pojo.vo.SiteProfileVO;
import com.blog.result.Result;
import com.blog.service.SiteProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端站点信息
 */
@Slf4j
@RestController("userSiteController")
@RequestMapping("/user/site")
public class SiteController {

    @Autowired
    private SiteProfileService siteProfileService;

    /**
     * 获取站点信息，未配置时返回空配置
     *
     * @return 站点信息
     */
    @GetMapping("/profile")
    public Result<SiteProfileVO> getProfile() {
        return Result.success(siteProfileService.getProfile());
    }
}
