package com.blog.utils;

import com.blog.pojo.vo.IpLocationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IpLocationUtil {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 判断是否内网IP，内网不调用定位接口
     */
    private boolean isInnerIp(String ip) {
        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        // 192.168 网段
        if (ip.startsWith("192.168.")) {
            return true;
        }
        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }
        // 172.16 ~ 172.31
        if (ip.startsWith("172.")) {
            String seg = ip.split("\\.")[1];
            int num = Integer.parseInt(seg);
            return num >= 16 && num <= 31;
        }
        return false;
    }

    /**
     * 根据公网IP获取省市位置
     */
    public String getIpLocation(String ip) {
        if (ip == null || ip.isEmpty() || isInnerIp(ip)) {
            return "内网本地";
        }
        try {
            String url = "https://ip-api.com/json/" + ip + "?lang=zh-CN";
            IpLocationVO vo = restTemplate.getForObject(url, IpLocationVO.class);
            if ("success".equals(vo.getStatus())) {
                return vo.getCountry() + " " + vo.getRegionName() + " " + vo.getCity();
            }
            return "未知地区";
        } catch (Exception e) {
            // 接口超时/访问失败统一返回
            return "定位失败";
        }
    }
}
