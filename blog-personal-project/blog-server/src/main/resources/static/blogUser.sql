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
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID,主键',
    `nickname`    varchar(50)  DEFAULT NULL COMMENT '昵称',
    `username`    varchar(30)     NOT NULL COMMENT '用户名',
    `phone`       varchar(20)  DEFAULT NULL COMMENT '手机号',
    `email`       varchar(50)  DEFAULT NULL COMMENT '邮箱',
    `sex`         tinyint      DEFAULT 2 COMMENT '性别 0=女 1=男 2=保密',
    `password`    varchar(100)    NOT NULL COMMENT '密码',
    `salt`        varchar(50)  DEFAULT NULL COMMENT '密码盐值',
    `avatar`      varchar(500) DEFAULT NULL COMMENT '头像',
    `intro`       varchar(500) DEFAULT NULL COMMENT '个人简介',
    `status`      tinyint      DEFAULT 1 COMMENT '账号状态 0=禁用 1=正常 2=注销',
    `role`        varchar(20)  DEFAULT 'user' COMMENT '角色:admin超级管理员/userAdmin管理员/user普通用户',
    `delete_flag` tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `website`     varchar(255) DEFAULT NULL COMMENT '个人网站',
    `github`      varchar(255) DEFAULT NULL COMMENT 'GitHub地址',
    `create_time` datetime     DEFAULT NULL COMMENT '注册时间',
    `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
    `login_ip`    varchar(50)  DEFAULT NULL COMMENT '最后登录IP',
    `login_agent` text         DEFAULT NULL COMMENT '登录浏览器Agent',
    `login_time`  datetime     DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user`
    DISABLE KEYS */;
INSERT INTO `user` (`id`, `nickname`, `username`, `phone`, `email`, `sex`, `password`, `salt`, `avatar`, `intro`,
                    `status`, `role`, `delete_flag`, `website`, `github`, `create_time`, `update_time`, `login_ip`,
                    `login_time`)
VALUES (1, '风起云涌', 'yesir', '18260780529', '2087691050@qq.com', 1, 'd4b1b8e62133128e9dcf8cb88ad3e860',
        '2ea3c4086b6c4787a2a3d703064764f3', 'https://blog-personal-yjl.oss-cn-beijing.aliyuncs.com/2026/05/29/userId-1/e49d3f33-29c0-4d79-8683-1dc4dae74af8.jpg', NULL, 1, 'admin', 0, NULL, NULL,
        '2023-06-01 17:08:23', '2023-06-01 17:39:58', NULL, NULL),
       (2, '李四', 'lisi', '13800138002', 'lisi@163.com', 1, '62bcbfd3ebdcfa23de02a4324520f2a9',
        'f3ab7bb323cb4880b1a861ca76998e56', 'https://picsum.photos/64/64', NULL, 1, 'user', 0, NULL, NULL,
        '2023-06-01 17:45:12', '2023-06-01 17:45:12', NULL, NULL),
       (3, '王芳', 'wangfang', '13800138003', '235478954@qq.com', 0, '718f710a768e836f7654cd1cac1be042',
        'c915b99e36e5496c91c3b707ac418af3', 'https://picsum.photos/64/65', NULL, 1, 'user', 0, NULL, NULL,
        '2023-06-01 17:45:40', '2023-06-01 17:45:40', NULL, NULL);
/*!40000 ALTER TABLE `user`
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