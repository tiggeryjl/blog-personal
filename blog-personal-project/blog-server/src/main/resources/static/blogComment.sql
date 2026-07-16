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
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment`
(
    `id`                  bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    `type`                tinyint         NOT NULL COMMENT '评论类型 0文章评论 1日常评论 2留言板',
    `source_id`           bigint          NOT NULL COMMENT '来源ID 文章ID/日常ID/0=留言板',

    `msg_type`            tinyint      DEFAULT 0 COMMENT '留言类型 0评论留言 1反馈建议 2申请友链',

    `parent_id`           bigint       DEFAULT 0 COMMENT '父评论ID 0=顶级评论',
    `reply_user_id`       bigint       DEFAULT 0 COMMENT '回复用户ID 0=没有',
    `reply_user_nickname` varchar(50)  DEFAULT NULL COMMENT '回复的用户昵称',

    `user_id`             bigint          NOT NULL COMMENT '评论人ID',
    `user_nickname`       varchar(50)  DEFAULT NULL COMMENT '评论人昵称',
    `user_avatar`         varchar(500) DEFAULT NULL COMMENT '评论人头像',

    `content`             text            NOT NULL COMMENT '评论内容',

    `like_num`            int          DEFAULT 0 COMMENT '点赞数',
    `status`              tinyint      DEFAULT 1 COMMENT '状态 0隐藏 1正常',
    `is_top`              tinyint      DEFAULT 0 COMMENT '是否置顶 0否 1是',

    `ip_address`          varchar(45)  DEFAULT NULL COMMENT '评论时IP地址',
    `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
    `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
    `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
    `location` varchar(100) DEFAULT NULL COMMENT '操作地理位置',
    `user_agent` text DEFAULT NULL COMMENT '原始UA头(浏览器User-Agent)',

    `delete_flag`         tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_type_source_del_status_top_time` (`type`, `source_id`, `delete_flag`, `status`, `is_top`, `create_time`),
    KEY `idx_parent_create_time` (`parent_id`, `create_time`),
    KEY `idx_user_delete_time` (`user_id`, `delete_flag`, `create_time`),
    KEY `idx_reply_user_id` (`reply_user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='公共评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment`
    DISABLE KEYS */;

INSERT INTO `comment`(id, type, source_id, msg_type, parent_id, reply_user_id,  user_id,
                      content, like_num, status, is_top, delete_flag, create_time, update_time)
VALUES
-- =============================================
-- 1. 文章评论（type=1, source_id=1）→ 一级评论
(1, 0, 1, 0, 0, 0, 1, '这篇文章写得非常详细，受益匪浅！', 8, 1, 0, 0,'2025-05-01 14:20:15', '2025-05-01 14:20:15'),
(2, 0, 1, 0, 1, 1, 1, '感谢支持，我会继续输出优质内容！', 5, 1, 0, 0,'2025-05-01 14:25:20', '2025-05-01 14:25:20'),

-- 回复评论1（二级评论）
(3, 0, 1, 0, 1, 1, 2, '说得对，确实很实用！', 3, 1, 0, 0,'2025-05-01 15:10:00', '2025-05-01 15:10:00'),

-- =============================================
-- 2. 日常评论（type=2, source_id=1）
(4, 1, 1, 0, 0, 0, 1, '记录生活超棒，加油！', 12, 1, 1, 0,'2025-05-02 10:00:00', '2025-05-02 10:00:00'),

-- 回复日常评论4
(5, 1, 1, 0, 4, 1, 3, '谢谢支持～', 2, 1, 0, 0,'2025-05-02 10:05:00', '2025-05-02 10:05:00'),

-- =============================================
-- 3. 留言板（type=3, source_id=0）
-- 留言 → 普通留言
(6, 2, 2, 0, 0, 0, 2, '来打卡留言，博主很棒！', 20, 1, 0, 0,'2025-05-03 09:30:00', '2025-05-03 09:30:00'),

-- 留言 → 反馈建议 msg_type=1
(7, 2, 3, 1, 0, 0, 3, '建议增加深色模式，夜间使用更舒服！', 8, 1, 0, 0,'2025-05-03 15:40:00', '2025-05-03 15:40:00'),

-- 留言 → 申请友链 msg_type=2
(8, 2, 2, 2, 0, 0, 1, '申请友链，我的博客是技术型，希望互换！', 0, 1, 0, 0,'2025-05-04 11:20:00', '2025-05-04 11:20:00');

/*!40000 ALTER TABLE `comment`
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