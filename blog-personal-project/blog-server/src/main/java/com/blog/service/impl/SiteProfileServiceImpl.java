package com.blog.service.impl;

import com.blog.constant.SystemConstant;
import com.blog.pojo.vo.SiteProfileVO;
import com.blog.service.SiteProfileService;
import com.blog.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 站点信息配置服务实现
 */
@Service
public class SiteProfileServiceImpl implements SiteProfileService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public SiteProfileVO getProfile() {
        SiteProfileVO profile = systemConfigService.getJsonValue(
                SystemConstant.SITE_PROFILE_CONFIG_KEY, SiteProfileVO.class);
        if (profile == null) {
            return SiteProfileVO.builder().name("").url("").avatar("").desc("").build();
        }
        return profile;
    }

    @Override
    public void saveProfile(SiteProfileVO profile) {
        SiteProfileVO save = SiteProfileVO.builder()
                .name(trim(profile.getName()))
                .url(trim(profile.getUrl()))
                .avatar(trim(profile.getAvatar()))
                .desc(trim(profile.getDesc()))
                .build();
        systemConfigService.saveJsonValue(
                SystemConstant.SITE_PROFILE_CONFIG_KEY, save, "站点信息（友链页展示）");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
