package com.blog.controller.admin;

import com.blog.pojo.vo.SiteProfileVO;
import com.blog.result.Result;
import com.blog.service.SiteProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 站点信息配置
 */
@Slf4j
@RestController("adminSiteController")
@RequestMapping("/admin/site")
public class SiteController {

    @Autowired
    private SiteProfileService siteProfileService;

    /**
     * 获取站点信息
     *
     * @return 站点信息
     */
    @PreAuthorize("hasPermission(null,'sys:notice:edit')")
    @GetMapping("/profile")
    public Result<SiteProfileVO> getProfile() {
        return Result.success(siteProfileService.getProfile());
    }

    /**
     * 保存站点信息
     *
     * @param profile 站点信息
     * @return 统一结果
     */
    @PreAuthorize("hasPermission(null,'sys:notice:edit')")
    @PutMapping("/profile")
    public Result saveProfile(@Valid @RequestBody SiteProfileVO profile) {
        log.info("保存站点信息:{}", profile.getName());
        siteProfileService.saveProfile(profile);
        return Result.success();
    }
}
