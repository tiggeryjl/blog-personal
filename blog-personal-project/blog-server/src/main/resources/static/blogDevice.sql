-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: blog
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Current Database: `blog`
--

CREATE DATABASE /*!32312 IF NOT EXISTS */ `blog`
    /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */
    /*!80016 DEFAULT ENCRYPTION = 'N' */;

USE `blog`;

DROP TABLE IF EXISTS `user_device`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_device`
(
    `id`              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint unsigned NOT NULL COMMENT '关联用户ID',
    `device_uuid`     varchar(128)  DEFAULT NULL COMMENT '前端生成设备唯一标识',
    `ip_address`      varchar(50)   DEFAULT NULL COMMENT '登录公网IP',
    `browser`         varchar(100)  DEFAULT NULL COMMENT '浏览器名称版本',
    `os`              varchar(100)  DEFAULT NULL COMMENT '操作系统',
    `device_type`     varchar(20)   DEFAULT NULL COMMENT '设备类型：电脑/手机/平板/未知',
    `location`        varchar(100)  DEFAULT NULL COMMENT 'IP解析地理位置(省市)',
    `user_agent`      text          DEFAULT NULL COMMENT '原始UA请求头',
    `is_trust`        tinyint NOT NULL DEFAULT 0 COMMENT '是否可信设备 0陌生 1可信',
    `last_login_time` datetime      DEFAULT NULL COMMENT '最近一次登录时间',
    `delete_flag`     tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time`     datetime      DEFAULT NULL COMMENT '设备首次绑定时间',
    `update_time`     datetime      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_device_uuid` (`user_id`, `device_uuid`) COMMENT '同一用户同一设备唯一',
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户登录设备表';
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log`
(
    `id`              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志主键ID',
    `oper_user_id`    bigint unsigned DEFAULT NULL COMMENT '操作人用户ID',
    `oper_username`   varchar(30)     DEFAULT NULL COMMENT '操作人用户名',
    `oper_nickname`   varchar(50)     DEFAULT NULL COMMENT '操作人昵称',
    `module_name`     varchar(50)     DEFAULT NULL COMMENT '操作模块(文章管理/用户管理/角色等)',
    `oper_title`      varchar(255)    DEFAULT NULL COMMENT '操作标题(新增文章、删除用户等)',
    `oper_type`       tinyint         DEFAULT NULL COMMENT '操作类型 1新增 2编辑 3删除 4查询 5导出 6导入',
    `request_url`     varchar(500)    DEFAULT NULL COMMENT '请求接口地址',
    `request_method`  varchar(20)     DEFAULT NULL COMMENT '请求方式GET/POST/PUT/DELETE',
    `ip_address`      varchar(50)     DEFAULT NULL COMMENT '操作IP',
    `browser`         varchar(100)    DEFAULT NULL COMMENT '浏览器',
    `os`              varchar(100)    DEFAULT NULL COMMENT '操作系统',
    `location`        varchar(100)    DEFAULT NULL COMMENT '操作地点',
    `oper_param`      text            DEFAULT NULL COMMENT '请求入参JSON',
    `oper_result`     text            DEFAULT NULL COMMENT '返回结果JSON',
    `cost_time`       bigint          DEFAULT NULL COMMENT '接口耗时(ms)',
    `status`          tinyint NOT NULL DEFAULT 1 COMMENT '操作状态 1成功 0失败',
    `error_msg`       text            DEFAULT NULL COMMENT '失败异常信息',
    `delete_flag`     tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time`     datetime        DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`oper_user_id`, `create_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='后台操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2024-08-28 21:54:00