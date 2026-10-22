package com.blog.service;

import com.blog.pojo.vo.SiteProfileVO;

/**
 * 站点信息配置服务
 */
public interface SiteProfileService {

    /**
     * 读取站点信息，未配置时返回空配置
     *
     * @return 站点信息
     */
    SiteProfileVO getProfile();

    /**
     * 保存站点信息
     *
     * @param profile 站点信息
     */
    void saveProfile(SiteProfileVO profile);
}
