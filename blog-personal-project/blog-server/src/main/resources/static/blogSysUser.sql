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
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user`
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
    `delete_flag` tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    `lock_flag`   tinyint      NOT NULL DEFAULT 0 COMMENT '账号锁定标识：0=未锁定，1=已锁定',
    `lock_time`   datetime     DEFAULT NULL COMMENT '账号锁定时间，用于定时自动解锁',
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
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user`
    DISABLE KEYS */;
INSERT INTO `sys_user` (`id`, `nickname`, `username`, `phone`, `email`, `sex`, `password`, `salt`, `avatar`, `intro`,
                    `status`, `delete_flag`,`lock_flag`,`lock_time`, `website`, `github`, `create_time`, `update_time`, `login_ip`,
                        `login_agent`,`login_time`)
VALUES (1, '风起云涌', 'yesir', '18260780529', '2087691050@qq.com', 1, 'd4b1b8e62133128e9dcf8cb88ad3e860',
        '2ea3c4086b6c4787a2a3d703064764f3', 'https://blog-personal-yjl.oss-cn-beijing.aliyuncs.com/2026/05/29/userId-1/e49d3f33-29c0-4d79-8683-1dc4dae74af8.jpg', NULL, 1,  0,0,NULL, NULL, NULL,
        '2023-06-01 17:08:23', '2023-06-01 17:39:58', NULL, NULL,NULL),
       (2, '李四', 'lisi', '13800138002', 'lisi@163.com', 1, '62bcbfd3ebdcfa23de02a4324520f2a9',
        'f3ab7bb323cb4880b1a861ca76998e56', 'https://picsum.photos/64/64', NULL, 1, 0,0,NULL, NULL, NULL,
        '2023-06-01 17:45:12', '2023-06-01 17:45:12', NULL, NULL,NULL),
       (3, '王芳', 'wangfang', '13800138003', '235478954@qq.com', 0, '718f710a768e836f7654cd1cac1be042',
        'c915b99e36e5496c91c3b707ac418af3', 'https://picsum.photos/64/65', NULL, 1,  0,0,NULL, NULL, NULL,
        '2023-06-01 17:45:40', '2023-06-01 17:45:40', NULL, NULL,NULL);
/*!40000 ALTER TABLE `sys_user`
    ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
    `role_name`   varchar(50)     NOT NULL COMMENT '角色名称',
    `role_key`    varchar(50)     NOT NULL COMMENT '角色唯一标识',
    `sort`        int             DEFAULT 0 COMMENT '角色排序',
    `remark`      varchar(255)    DEFAULT NULL COMMENT '角色备注说明',
    `status`      tinyint NOT NULL DEFAULT 1 COMMENT '角色状态：0禁用 1正常',
    `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time` datetime        DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

-- 初始化3个角色
LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role`
    DISABLE KEYS */;
INSERT INTO `sys_role` (`role_name`, `role_key`, `sort`, `remark`,`status`,`delete_flag`, `create_time`, `update_time`)
VALUES ('超级管理员', 'admin', 1, '拥有系统所有模块全部权限', 1,0,NOW(), NOW()),
       ('业务管理员', 'operator', 2, '可管理文章、作品、分类、评论、友链，不可操作后台用户', 1,0,NOW(), NOW()),
       ('数据分析员', 'viewer', 3, '仅所有模块查看权限，无新增编辑删除操作权限', 1,0,NOW(), NOW());
/*!40000 ALTER TABLE `sys_role`
    ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint unsigned NOT NULL COMMENT '后台用户ID',
    `role_id`     bigint unsigned NOT NULL COMMENT '角色ID',
    `create_time` datetime        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '同一个用户不能重复绑定同一个角色',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户-角色关联中间表';
/*!40101 SET character_set_client = @saved_cs_client */;

-- 给原有3个用户分配初始角色
LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role`
    DISABLE KEYS */;
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`)
VALUES (1, 1, NOW()), -- yesir 超级管理员
       (2, 2, NOW()), -- lisi 业务管理员
       (3, 3, NOW()); -- wangfang 普通浏览员
/*!40000 ALTER TABLE `sys_user_role`
    ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
    `parent_id`   bigint unsigned DEFAULT 0 COMMENT '父菜单ID，0代表顶级菜单',
    `menu_name`   varchar(50)     NOT NULL COMMENT '菜单显示名称',
    `path`        varchar(255)    DEFAULT NULL COMMENT '前端路由访问地址',
    `component`   varchar(255)    DEFAULT NULL COMMENT '前端页面组件路径',
    `perms`       varchar(100)    DEFAULT NULL COMMENT '权限标识符，接口、按钮鉴权使用',
    `menu_type`   tinyint         NOT NULL COMMENT '菜单类型：0=目录 1=页面菜单 2=按钮权限',
    `icon`        varchar(255)    DEFAULT NULL COMMENT '菜单图标',
    `sort`        int             DEFAULT 0 COMMENT '菜单排序',
    `status`      tinyint NOT NULL DEFAULT 1 COMMENT '角色状态：0禁用 1正常',
    `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    `create_time` datetime        DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     bigint unsigned NOT NULL COMMENT '角色ID',
    `menu_id`     bigint unsigned NOT NULL COMMENT '菜单权限ID',
    `create_time` datetime        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`) COMMENT '角色不能重复绑定同一个菜单权限',
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色-菜单权限关联中间表';
/*!40101 SET character_set_client = @saved_cs_client */;


LOCK TABLES `sys_menu` WRITE, `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `perms`, `menu_type`, `icon`, `sort`,`status`,`delete_flag`,`create_time`, `update_time`)
VALUES
-- ===================== 1、首页（顶级页面，无父级） =====================
(1, 0, '首页', '/index', 'index', 'sys:index:list', 0, 'Promotion', 1,1, 0,NOW(), NOW()),

-- ===================== 2、作品管理【顶级目录 parent_id=0】 =====================
(2, 0, '作品管理', '/artwork', 'Layout', NULL, 0, 'HelpFilled', 2, 1,0,NOW(), NOW()),
-- 文章管理页面
(3, 2, '文章管理', '/articleWork', 'artwork/articleWork/index', 'sys:article:list', 1, 'Document', 1, 1,0,NOW(), NOW()),
(4, 3, '新增文章', NULL, NULL, 'sys:article:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(5, 3, '编辑文章', NULL, NULL, 'sys:article:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(6, 3, '删除文章', NULL, NULL, 'sys:article:delete', 2, NULL, 3, 1,0,NOW(), NOW()),
-- 日常管理页面
(7, 2, '日常管理', '/dailyWork', 'artwork/dailyWork/index', 'sys:works:list', 1, 'Notebook', 2, 1,0,NOW(), NOW()),
(8, 7, '新增日常', NULL, NULL, 'sys:works:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(9, 7, '编辑日常', NULL, NULL, 'sys:works:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(10,7, '删除日常', NULL, NULL, 'sys:works:delete', 2, NULL, 3, 1,0,NOW(), NOW()),
-- 分类标签页面
(11,2, '分类标签', '/category-tag', 'artwork/categoryOrTag/index', 'sys:category:list', 1, 'Discount', 3, 1,0,NOW(), NOW()),
(12,11,'新增分类', NULL, NULL, 'sys:category:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(13,11,'编辑分类', NULL, NULL, 'sys:category:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(14,11,'删除分类', NULL, NULL, 'sys:category:delete', 2, NULL, 3, 1,0,NOW(), NOW()),

-- ===================== 3、评论管理【顶级目录 parent_id=0】 =====================
(15,0, '评论管理', '/comment', 'Layout', NULL, 0, 'Comment', 3, 1,0,NOW(), NOW()),
-- 文章评论
(16,15,'文章评论', '/article-comment', 'comment/articleComment/index', 'sys:comment:article:list', 1, 'Document', 1, 1,0,NOW(), NOW()),
(17,16,'审核评论', NULL, NULL, 'sys:comment:audit', 2, NULL, 1, 1,0,NOW(), NOW()),
(18,16,'删除评论', NULL, NULL, 'sys:comment:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 日常评论
(19,15,'日常评论', '/daily-comment', 'comment/dailyComment/index', 'sys:comment:daily:list', 1, 'Notebook', 2, 1,0,NOW(), NOW()),
(20,19,'审核评论', NULL, NULL, 'sys:comment:audit', 2, NULL, 1, 1,0,NOW(), NOW()),
(21,19,'删除评论', NULL, NULL, 'sys:comment:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 留言评论
(22,15,'留言评论', '/message-comment', 'comment/messageComment/index', 'sys:comment:message:list', 1, 'ChatLineSquare', 3, 1,0,NOW(), NOW()),
(23,22,'审核留言', NULL, NULL, 'sys:comment:audit', 2, NULL, 1, 1,0,NOW(), NOW()),
(24,22,'删除留言', NULL, NULL, 'sys:comment:delete', 2, NULL, 2, 1,0,NOW(), NOW()),

-- ===================== 4、用户管理【顶级目录 parent_id=0】 =====================
(25,0, '用户管理', '/user', 'Layout', NULL, 0, 'UserFilled', 4, 1,0,NOW(), NOW()),
(26,25,'用户信息', '/userInfo', 'user/index', 'sys:user:list', 1, 'InfoFilled', 1, 1,0,NOW(), NOW()),
(27,26,'新增用户', NULL, NULL, 'sys:user:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(28,26,'编辑用户', NULL, NULL, 'sys:user:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(29,26,'删除用户', NULL, NULL, 'sys:user:delete', 2, NULL, 3, 1,0,NOW(), NOW()),
(30,26,'分配角色', NULL, NULL, 'sys:user:role', 2, NULL, 4, 1,0,NOW(), NOW()),

-- ===================== 5、友链管理【顶级目录 parent_id=0】 =====================
(31,0, '友链管理', '/link', 'Layout', NULL, 0, 'Share', 5, 1,0,NOW(), NOW()),
(32,31,'友链信息', '/linkInfo', 'link/index', 'sys:link:list', 1, 'Connection', 1, 1,0,NOW(), NOW()),
(33,32,'新增友链', NULL, NULL, 'sys:link:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(34,32,'编辑友链', NULL, NULL, 'sys:link:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(35,32,'删除友链', NULL, NULL, 'sys:link:delete', 2, NULL, 3, 1,0,NOW(), NOW()),

-- ===================== 6、数据分析【顶级目录 parent_id=0】 =====================
(36,0, '数据分析', '/report', 'Layout', NULL, 0, 'Histogram', 6, 1,0,NOW(), NOW()),
(37,36,'作品信息统计', '/artworkReport', 'report/artworkReport/index', 'sys:report:works:list', 1, 'Document', 1, 1,0,NOW(), NOW()),
(38,36,'评论信息统计', '/commentReport', 'report/commentReport/index', 'sys:report:comment:list', 1, 'ChatLineSquare', 2, 1,0,NOW(), NOW()),
(39,36,'用户信息统计', '/userReport', 'report/userReport/index', 'sys:report:user:list', 1, 'User', 3, 1,0,NOW(), NOW()),
(40,36,'友链信息统计', '/linkReport', 'report/linkReport/index', 'sys:report:link:list', 1, 'Share', 4, 1,0,NOW(), NOW()),

-- ===================== 7、布局设置【顶级目录 parent_id=0】 =====================
(41,0, '布局设置', '/meunlayout', 'Layout', NULL, 0, 'Menu', 7, 1,0,NOW(), NOW()),
(42,41,'布局设计', '/meunlayout', 'meunlayout/index', 'sys:layout:list', 1, 'InfoFilled', 1, 1,0,NOW(), NOW()),

-- ===================== 8、系统管理（角色管理，超级管理员专属） =====================
(43,0, '系统管理', '/system', 'Layout', 'sys:manage', 0, 'Tools', 8, 1,0,NOW(), NOW()),
(44,43,'角色管理', '/role', 'system/role/index', 'sys:role:list', 1, 'Coin', 1, 1,0,NOW(), NOW()),
(45,44,'新增角色', NULL, NULL, 'sys:role:add', 2, NULL, 1, 1,0,NOW(), NOW()),
(46,44,'编辑角色', NULL, NULL, 'sys:role:edit', 2, NULL, 2, 1,0,NOW(), NOW()),
(47,44,'删除角色', NULL, NULL, 'sys:role:delete', 2, NULL, 3, 1,0,NOW(), NOW()),
(48,44,'分配权限', NULL, NULL, 'sys:role:assign', 2, NULL, 4, 1,0,NOW(), NOW()),

(49,43,'权限管理','/menu','system/menu/index','sys:menu:list',1,'Key',2,1,0,NOW(),NOW()),
(50,49,'新增权限',NULL,NULL,'sys:menu:add',2,NULL,1,1,0,NOW(),NOW()),
(51,49,'编辑权限',NULL,NULL,'sys:menu:edit',2,NULL,2,1,0,NOW(),NOW()),
(52,49,'删除权限',NULL,NULL,'sys:menu:delete',2,NULL,3,1,0,NOW(),NOW()),

-- ===================== 9、回收站管理（超级管理员专属） =====================
(53,0, '回收站管理', '/recycleBin', 'Layout', 'sys:recycle', 0, 'Delete', 9, 1,0,NOW(), NOW()),
(54,43,'权限回收站', '/recycleMenu', 'recycleBin/menu/index', 'sys:recycleMenu:list', 1, 'Delete', 1, 1,0,NOW(), NOW()),
(55,43,'角色回收站', '/recycleRole', 'recycleBin/role/index', 'sys:recycleRole:list', 1, 'Delete', 2, 1,0,NOW(), NOW()),
(56,44,'恢复权限', NULL, NULL, 'sys:recycleMenu:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(57,44,'删除权限', NULL, NULL, 'sys:recycleMenu:delete', 2, NULL, 2, 1,0,NOW(), NOW())
;

-- 1、超级管理员(role_id=1)：绑定全部菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, id, NOW() FROM sys_menu;

-- 2、运营管理员(role_id=2)：只绑定内容管理、日常管理模块，排除【系统管理】（用户、角色管理）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 2, id, NOW() FROM sys_menu WHERE id <> 43 AND parent_id <> 43;

-- 3、浏览员(role_id=3)：只绑定所有页面查询权限，不绑定任何按钮操作权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 3, id, NOW() FROM sys_menu WHERE menu_type IN (0,1);

/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
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