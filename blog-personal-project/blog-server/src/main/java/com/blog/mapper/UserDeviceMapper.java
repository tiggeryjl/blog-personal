package com.blog.mapper;

import com.blog.pojo.entity.UserDevice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户登录设备数据访问
 */
@Mapper
public interface UserDeviceMapper {

    /**
     * 查询指定设备是否已经通过邮箱验证并标记为可信
     */
    @Select("""
            SELECT COUNT(*)
            FROM user_device
            WHERE user_id = #{userId}
              AND device_uuid = #{deviceId}
              AND is_trust = 1
              AND delete_flag = 0
            """)
    int countTrustedDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 新增或更新可信设备
     */
    @Insert("""
            INSERT INTO user_device(
                user_id, device_uuid, ip_address, browser, os, device_type,
                location, user_agent, is_trust, last_login_time, delete_flag,
                create_time, update_time
            )
            VALUES(
                #{userId}, #{deviceUuid}, #{ipAddress}, #{browser}, #{os}, #{deviceType},
                #{location}, #{userAgent}, 1, #{lastLoginTime}, 0,
                #{createTime}, #{updateTime}
            )
            ON DUPLICATE KEY UPDATE
                ip_address = VALUES(ip_address),
                browser = VALUES(browser),
                os = VALUES(os),
                device_type = VALUES(device_type),
                location = VALUES(location),
                user_agent = VALUES(user_agent),
                is_trust = 1,
                last_login_time = VALUES(last_login_time),
                delete_flag = 0,
                update_time = VALUES(update_time)
            """)
    int upsertTrustedDevice(UserDevice userDevice);
}
