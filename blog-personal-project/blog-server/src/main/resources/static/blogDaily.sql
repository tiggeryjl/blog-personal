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

-- ============================================================
-- ALTER TABLE `daily`
--     ADD COLUMN `timed_publish_time` datetime DEFAULT NULL COMMENT '定时发布时间' AFTER `publish_time`,
--     ADD KEY `idx_timed_publish_time` (`timed_publish_time`);
-- ALTER TABLE `daily`
--     ADD COLUMN `view_num` int DEFAULT 0 COMMENT '浏览数' AFTER `comment_num`;
-- ============================================================

--
-- Table structure for table `daily`
--

DROP TABLE IF EXISTS `daily`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    `content`       longtext      DEFAULT NULL COMMENT '日常内容',
    `images`        varchar(1024) DEFAULT NULL COMMENT '图片地址，多个用英文逗号分隔',
    `files`         varchar(1024) DEFAULT NULL COMMENT '文件地址，多个用英文逗号分隔',

    `user_id`       bigint          NOT NULL COMMENT '发布者ID',
    `user_nickname` varchar(50)   DEFAULT NULL COMMENT '作者昵称',
    `user_avatar`   varchar(255)  DEFAULT NULL COMMENT '作者头像',

    `type`          tinyint       DEFAULT 0 COMMENT '类型 0纯文字 1图片 2文件 3图文混合',
    `is_top`        tinyint       DEFAULT 0 COMMENT '是否置顶 0否 1是',
    `status`        tinyint       DEFAULT 1 COMMENT '状态 0草稿 1已发布 2已下架 3定时发布 4私密',

    `delete_flag`   tinyint       DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `sort`          int           DEFAULT 0 COMMENT '排序权重',
    `like_num`      int           DEFAULT 0 COMMENT '点赞数',
    `comment_num`   int           DEFAULT 0 COMMENT '评论数',
    `view_num`      int           DEFAULT 0 COMMENT '浏览数',

    `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
    `publish_time`  datetime      DEFAULT NULL COMMENT '发布时间',
    `timed_publish_time` datetime DEFAULT NULL COMMENT '定时发布时间',
    `update_time`   datetime      DEFAULT NULL COMMENT '修改时间',

    PRIMARY KEY (`id`),
    KEY `idx_status_del_top_pubtime` (`status`, `delete_flag`, `is_top`, `publish_time`),
    KEY `idx_user_del_pubtime` (`user_id`, `delete_flag`, `publish_time`),
    KEY `idx_status_pubtime` (`status`, `publish_time`),
    KEY `idx_timed_publish_time` (`timed_publish_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='日常记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `daily` WRITE;
/*!40000 ALTER TABLE `daily`
    DISABLE KEYS */;
INSERT INTO `daily`(id, content, images, files, user_id, type, is_top, status,delete_flag, sort,
                    like_num, comment_num, create_time,publish_time, update_time)
VALUES (1, '今天天气不错，分享一下生活～', 'https://picsum.photos/200/200,https://picsum.photos/201/201', '', 1,
        1, 1, 1,0, 1000, 10, 2, '2025-05-01 14:30:00','2025-05-01 14:30:00', '2025-05-01 14:30:00'),
       (2, '分享学习笔记和资料', 'https://picsum.photos/300/300',
        'https://xxx.oss-cn-beijing.aliyuncs.com/file/笔记.pdf,https://xxx.oss-cn-beijing.aliyuncs.com/file/教程.docx',
        1, 3, 0, 1, 0,2000, 5, 1, '2025-05-02 16:20:00', '2025-05-01 14:30:00','2025-05-02 16:20:00'),
       (3, '纯文字日常，记录心情！', '', '', 1, 0, 1, 0,0, 3000, 2, 0, '2025-05-01 14:30:00','2025-05-03 10:10:00',
        '2025-05-03 10:10:00'),
       (4, '周末出游照片合集',
        'https://picsum.photos/400/400,https://picsum.photos/401/401,https://picsum.photos/402/402', '', 1,
        1, 1, 1,0, 1500, 88, 12, '2025-05-04 09:05:00', '2025-05-01 14:30:00','2025-05-04 09:05:00'),
       (5, '私密记录，不对外公开', '', 'https://xxx.oss-cn-beijing.aliyuncs.com/file/私人文件.zip', 1, 2, 0,
        0,0, 2500, 0, 0, '2025-05-05 20:00:00', '2025-05-01 14:30:00','2025-05-05 20:00:00');
/*!40000 ALTER TABLE `daily`
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
