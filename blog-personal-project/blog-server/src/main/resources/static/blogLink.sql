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

--
-- Table structure for table `link`
--

DROP TABLE IF EXISTS `link`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `link`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `link_name`    varchar(50)     NOT NULL COMMENT '网站名称',
    `link_url`     varchar(255)    NOT NULL COMMENT '网站链接',
    `link_avatar`  varchar(500) DEFAULT NULL COMMENT '站点头像图标',
    `link_desc`    varchar(200) DEFAULT NULL COMMENT '站点简介',
    `link_email`   varchar(100) DEFAULT NULL COMMENT '申请邮箱',
    `sort`         int          DEFAULT 0 COMMENT '排序权重',
    `audit_status` tinyint      DEFAULT 0 COMMENT '审核状态 0待审核 1审核通过 2审核拒绝',
    `status`       tinyint      DEFAULT 0 COMMENT '展示状态 0禁用 1正常展示',
    `delete_flag`  tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time`  datetime     DEFAULT NULL COMMENT '添加时间',
    `update_time`  datetime     DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_link_url_del` (`link_url`, `delete_flag`),
    KEY `idx_audit_status_del_sort_time` (`audit_status`, `status`, `delete_flag`, `sort`, `create_time`),
    KEY `idx_audit_del_time` (`audit_status`, `delete_flag`, `create_time`),
    KEY `idx_audit_del_sort_time` (`audit_status`, `delete_flag`, `sort`, `create_time`),
    KEY `idx_link_name` (`link_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='友情链接表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `link` WRITE;
/*!40000 ALTER TABLE `link`
    DISABLE KEYS */;
INSERT INTO `link` (`id`, `link_name`, `link_url`, `link_avatar`, `link_desc`, `link_email`, `sort`, `audit_status`, `status`,
                    `delete_flag`, `create_time`, `update_time`)
VALUES (1, '技术小屋', 'https://demo1.com', 'https://picsum.photos/80/80', '专注后端与前端技术分享', 'demo1@qq.com', 1, 1, 1, 0,
        '2025-05-01 10:20:15', '2025-05-02 09:10:22'),
       (2, '日常随笔', 'https://demo2.com', 'https://picsum.photos/81/81', '记录生活点滴与学习感悟', 'demo2@qq.com', 2, 1, 1, 0,
        '2025-05-03 14:30:08', '2025-05-03 14:30:08'),
       (3, '开源分享站', 'https://demo3.com', 'https://picsum.photos/82/82', '各类开源项目教程汇总', 'test1@qq.com', 3, 0, 0, 0,
        '2025-05-20 08:15:36', '2025-05-20 08:15:36'),
       (4, '设计美学馆', 'https://demo4.com', 'https://picsum.photos/83/83', 'UI设计与视觉创意展示', 'test2@qq.com', 4, 2, 0, 0,
        '2025-05-22 16:40:52', '2025-05-22 16:40:52'),
       (5, '编程知识库', 'https://demo5.com', 'https://picsum.photos/84/84', '零基础编程入门教学', 'demo5@qq.com', 5, 1, 0, 0,
        '2025-05-23 11:05:18', '2025-05-23 11:05:18');
/*!40000 ALTER TABLE `link`
    ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2024-08-28 21:54:00
