package com.blog.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接收IP回传的地址信息vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpLocationVO {
    // success/fail
    private String status;
    private String country;
    private String regionName;
    private String city;
    // 运营商
    private String isp;
}
