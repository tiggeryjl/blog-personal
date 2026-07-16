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
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID,主键',
    `title`         varchar(200)    NOT NULL COMMENT '文章标题',
    `summary`       varchar(300) DEFAULT NULL COMMENT '文章摘要',
    `content`       longtext        NOT NULL COMMENT '文章内容',

    `category_id`   bigint       DEFAULT NULL COMMENT '分类ID',
    `cover`         varchar(255) DEFAULT NULL COMMENT '封面图片',

    `status`        tinyint      DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已下架 3已归档 4定时发布 5私密',
    `is_top`        tinyint      DEFAULT 0 COMMENT '是否置顶 0否 1是',
    `is_hot`        tinyint      DEFAULT 0 COMMENT '是否热门 0否 1是',

    `view_num`      int          DEFAULT 0 COMMENT '阅读量',
    `like_num`      int          DEFAULT 0 COMMENT '点赞数',
    `comment_num`   int          DEFAULT 0 COMMENT '评论数',

    `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime     DEFAULT NULL COMMENT '修改时间',
    `publish_time`  datetime     DEFAULT NULL COMMENT '发布时间',

    `user_id`       bigint       DEFAULT NULL COMMENT '作者ID',
    `user_nickname` varchar(50)  DEFAULT NULL COMMENT '作者昵称',
    `user_avatar`   varchar(255) DEFAULT NULL COMMENT '作者头像',

    `delete_flag`   tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `sort`          int          DEFAULT 0 COMMENT '排序权重',

    PRIMARY KEY (`id`),
    KEY `idx_status_del_top_pubtime` (`status`, `delete_flag`, `is_top`, `publish_time`),
    KEY `idx_category_status_del_pubtime` (`category_id`, `status`, `delete_flag`, `publish_time`),
    KEY `idx_user_status_del_pubtime` (`user_id`, `status`, `delete_flag`, `publish_time`),
    KEY `idx_status_del_pubtime` (`status`, `delete_flag`, `publish_time`),
    KEY `idx_status_publishtime` (`status`, `publish_time`),
    KEY `idx_status_hot_del_pubtime` (`status`, `is_hot`, `delete_flag`, `publish_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article`
    DISABLE KEYS */;
INSERT INTO `article` (`id`, `title`, `summary`, `content`, `category_id`, `cover`, `status`,
                       `is_top`, `is_hot`, `view_num`, `like_num`, `comment_num`, `create_time`, `update_time`,
                       `publish_time`, `user_id`, `delete_flag`, `sort`)
VALUES (1, 'Vue3 + ElementPlus后台管理实战', '从零搭建个人博客后端管理页面，实现文章增删改查',
        '本篇讲解Vue3组合式API、路由配置、表单校验、表格封装等核心后台功能，适配个人博客项目开发', 1,
        'https://picsum.photos/400/220', 1, 1, 1, 1268, 86, 23, '2025-01-01 12:00:00',
        '2025-01-05 09:22:16', '2025-01-01 14:30:00', 1, 0, 99),
       (2, '个人博客开发日记', '记录从选型到上线完整开发流程',
        '选用SpringBoot+Vue3技术栈，完成数据库设计、接口编写、页面布局与部署上线全过程', 2,
        'https://picsum.photos/400/221', 0, 1, 0, 326, 18, 5, '2025-01-02 15:30:00', '2025-01-02 15:30:00', NULL, 1,
        0, 10),
       (3, 'MySQL常用查询优化技巧', '日常开发SQL语句调优实用方法',
        '索引创建、联表查询、分页优化、避免全表扫描等实战优化方案', 1,
        'https://picsum.photos/400/222', 2, 0, 0, 562, 41, 12, '2025-01-03 10:20:00', '2025-01-06 11:10:25',
        '2025-01-03 16:10:00', 1, 0, 20),
       (4, 'JavaScript进阶语法指南', '梳理ES6+常用语法与实战用法',
        '箭头函数、解构赋值、异步Promise、async/await等前端高频知识点讲解', 1,
        'https://picsum.photos/400/223', 3, 1, 1, 895, 63, 17, '2025-01-04 09:10:00', '2025-01-07 14:05:32',
        '2025-01-04 11:20:00', 1, 0, 80),
       (5, '日常随笔：生活感悟', '平淡生活中的思考与记录', '记录工作学习之余日常心境，分享成长路上点滴心得', 3,
        'https://picsum.photos/400/224', 5, 0, 0, 218, 9, 3, '2025-01-05 16:40:00', '2025-01-05 16:40:00',
        '2025-01-05 18:00:00', 1, 0, 5),
       (6, 'SpringBoot接口统一封装', '后端接口规范化统一返回格式',
        '全局异常处理、参数校验、响应体封装，规范项目后端接口输出', 1,
        'https://picsum.photos/400/225', 4, 0, 0, 0, 0, 0, '2025-01-06 08:30:00', '2025-01-06 08:30:00',
        '2025-06-01 10:00:00', 1, 0, 15);
/*!40000 ALTER TABLE `article`
    ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        varchar(50)     NOT NULL COMMENT '分类名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `status`      tinyint      DEFAULT 0 COMMENT '状态 0=禁用 1=正常',
    `sort`        int          DEFAULT 0 COMMENT '排序',
    `delete_flag` tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time` datetime     DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='分类表';

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        varchar(30)     NOT NULL COMMENT '标签名称',
    `status`      tinyint  DEFAULT 0 COMMENT '状态 0=禁用 1=正常',
    `delete_flag` tinyint  DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='标签表';

DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT,
    `article_id`  bigint          NOT NULL COMMENT '文章ID',
    `tag_id`      bigint          NOT NULL COMMENT '标签ID',
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章标签关联表';

DROP TABLE IF EXISTS `user_like`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_like`
(
    `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint NOT NULL COMMENT '点赞用户ID',
    `target_type`   tinyint NOT NULL DEFAULT 1 COMMENT '点赞对象类型 0=文章 1=日常 2=评论',
    `target_id`     bigint NOT NULL COMMENT '点赞目标ID(文章ID/日常ID/评论ID)',
    `delete_flag`   tinyint DEFAULT 0 COMMENT '逻辑删除 0=正常 1=取消点赞',
    `create_time`   datetime DEFAULT NULL COMMENT '点赞时间',
    `update_time`   datetime DEFAULT NULL COMMENT '更新时间(取消点赞更新)',
    PRIMARY KEY (`id`),
    -- 联合唯一索引：同一用户不能重复点赞同一个目标
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`, `delete_flag`),
    -- 快速查询某篇文章所有点赞记录
    KEY `idx_target_type_id_del` (`target_type`, `target_id`, `delete_flag`),
    -- 查询当前用户所有点赞内容
    KEY `idx_user_del` (`user_id`, `delete_flag`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `article_view_record`;
CREATE TABLE `article_view_record` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` bigint unsigned NOT NULL COMMENT '文章ID',
    `user_id` bigint unsigned DEFAULT NULL COMMENT '访客用户ID，未登录为null',
    `ip_address` varchar(50) NOT NULL COMMENT '访问IP',
    `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
    `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
    `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
    `location` varchar(100) DEFAULT NULL COMMENT '访问地点',
    `user_agent` text COMMENT 'UA原始信息',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    `view_date` date GENERATED ALWAYS AS (DATE(create_time)) STORED COMMENT '浏览日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_day_view` (`article_id`, `user_id`, `ip_address`, `view_date`),
    KEY `idx_article_time` (`article_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章每日浏览记录表';

/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2024-08-28 21:54:00