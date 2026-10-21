-- 博客个人站 · 数据库初始化脚本
--   1. 仅包含表结构与运行必需的基础数据
--   2. 默认管理员账号：admin / 123456，首次登录后请立即修改
--   注意用户端和管理端共用一个用户表，采用RBAC权限分表，仅被赋予管理员角色的用户可登录管理端

CREATE DATABASE IF NOT EXISTS blog;

USE blog;

-- 一、账号与权限
-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID,主键',
    nickname    varchar(50)  DEFAULT NULL COMMENT '昵称',
    username    varchar(30)     NOT NULL COMMENT '用户名',
    phone       varchar(20)  DEFAULT NULL COMMENT '手机号',
    email       varchar(50)  DEFAULT NULL COMMENT '邮箱',
    sex         tinyint      DEFAULT 2 COMMENT '性别 0=女 1=男 2=保密',
    password    varchar(100)    NOT NULL COMMENT '密码',
    salt        varchar(50)  DEFAULT NULL COMMENT '密码盐值',
    avatar      varchar(500) DEFAULT NULL COMMENT '头像',
    intro       varchar(500) DEFAULT NULL COMMENT '个人简介',
    status      tinyint      DEFAULT 1 COMMENT '账号状态 0=禁用 1=正常 2=注销',
    delete_flag tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    lock_flag   tinyint      NOT NULL DEFAULT 0 COMMENT '账号锁定标识：0=未锁定，1=已锁定',
    lock_time   datetime     DEFAULT NULL COMMENT '账号锁定时间，用于定时自动解锁',
    website     varchar(255) DEFAULT NULL COMMENT '个人网站',
    github      varchar(255) DEFAULT NULL COMMENT 'GitHub地址',
    create_time datetime     DEFAULT NULL COMMENT '注册时间',
    update_time datetime     DEFAULT NULL COMMENT '修改时间',
    login_ip    varchar(50)  DEFAULT NULL COMMENT '最后登录IP',
    login_agent text         DEFAULT NULL COMMENT '登录浏览器Agent',
    login_time  datetime     DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email)
) COMMENT ='用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
    role_name   varchar(50)     NOT NULL COMMENT '角色名称',
    role_key    varchar(50)     NOT NULL COMMENT '角色唯一标识',
    sort        int             DEFAULT 0 COMMENT '角色排序',
    remark      varchar(255)    DEFAULT NULL COMMENT '角色备注说明',
    status      tinyint NOT NULL DEFAULT 1 COMMENT '角色状态：0禁用 1正常',
    delete_flag tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    create_time datetime        DEFAULT NULL COMMENT '创建时间',
    update_time datetime        DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
)  COMMENT ='系统角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     bigint unsigned NOT NULL COMMENT '后台用户ID',
    role_id     bigint unsigned NOT NULL COMMENT '角色ID',
    create_time datetime        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id) COMMENT '同一个用户不能重复绑定同一个角色',
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) COMMENT ='用户-角色关联中间表';

-- 菜单权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
    parent_id   bigint unsigned DEFAULT 0 COMMENT '父菜单ID，0代表顶级菜单',
    menu_name   varchar(50)     NOT NULL COMMENT '菜单显示名称',
    path        varchar(255)    DEFAULT NULL COMMENT '前端路由访问地址',
    component   varchar(255)    DEFAULT NULL COMMENT '前端页面组件路径',
    perms       varchar(100)    DEFAULT NULL COMMENT '权限标识符，接口、按钮鉴权使用',
    menu_type   tinyint         NOT NULL COMMENT '菜单类型：0=目录 1=页面菜单 2=按钮权限',
    icon        varchar(255)    DEFAULT NULL COMMENT '菜单图标',
    sort        int             DEFAULT 0 COMMENT '菜单排序',
    status      tinyint NOT NULL DEFAULT 1 COMMENT '角色状态：0禁用 1正常',
    delete_flag tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    create_time datetime        DEFAULT NULL COMMENT '创建时间',
    update_time datetime        DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) COMMENT ='系统菜单权限表';

-- 角色菜单权限关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     bigint unsigned NOT NULL COMMENT '角色ID',
    menu_id     bigint unsigned NOT NULL COMMENT '菜单权限ID',
    create_time datetime        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id) COMMENT '角色不能重复绑定同一个菜单权限',
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) COMMENT ='角色-菜单权限关联中间表';

-- 后台通知表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    type VARCHAR(20) NOT NULL COMMENT '事件类型：like 点赞 / comment 评论 / link 友链',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    action_text VARCHAR(50) NOT NULL COMMENT '动作文案：点赞 / 评论 / 回复评论 / 申请',
    target_type VARCHAR(20) NOT NULL COMMENT '跳转目标类型：article / daily / link',
    target_title VARCHAR(255) NOT NULL COMMENT '跳转目标标题或摘要',
    target_id BIGINT NOT NULL COMMENT '跳转目标ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '访客昵称',
    content TEXT COMMENT '评论内容，点赞为空',
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_read_time (is_read, create_time DESC),
    INDEX idx_target_type_id (target_type, target_id)
) COMMENT ='后台事件通知记录表';

-- 二、站点配置
-- 系统配置表
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config
(
    id bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    config_key varchar(50) NOT NULL COMMENT '配置键',
    config_value text DEFAULT NULL COMMENT '配置值',
    config_type varchar(20) DEFAULT NULL COMMENT '配置类型,string,number,boolean,json,date',
    description varchar(255) DEFAULT NULL COMMENT '配置描述',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) COMMENT='系统配置表';

-- 三、内容相关

-- 分类表
DROP TABLE IF EXISTS category;
CREATE TABLE category
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name        varchar(50)     NOT NULL COMMENT '分类名称',
    description varchar(255) DEFAULT NULL COMMENT '描述',
    status      tinyint      DEFAULT 0 COMMENT '状态 0=禁用 1=正常',
    sort        int          DEFAULT 0 COMMENT '排序',
    delete_flag tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    create_time datetime     DEFAULT NULL,
    update_time datetime     DEFAULT NULL,
    PRIMARY KEY (id)
) COMMENT ='分类表';

-- 标签表
DROP TABLE IF EXISTS tag;
CREATE TABLE tag
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name        varchar(30)     NOT NULL COMMENT '标签名称',
    status      tinyint  DEFAULT 0 COMMENT '状态 0=禁用 1=正常',
    delete_flag tinyint  DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) COMMENT ='标签表';

-- 文章表
DROP TABLE IF EXISTS article;
CREATE TABLE article
(
    id            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID,主键',
    title         varchar(200)    NOT NULL COMMENT '文章标题',
    summary       varchar(300) DEFAULT NULL COMMENT '文章摘要',
    ai_suggest      TINYINT(1) DEFAULT 0 COMMENT '摘要是否AI生成 0否 1是',
    content       longtext        NOT NULL COMMENT '文章内容',

    category_id   bigint       DEFAULT NULL COMMENT '分类ID',
    cover         varchar(255) DEFAULT NULL COMMENT '封面图片',

    status        tinyint      DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已下架 3已归档 4定时发布 5私密',
    is_top        tinyint      DEFAULT 0 COMMENT '是否置顶 0否 1是',
    is_hot        tinyint      DEFAULT 0 COMMENT '是否热门 0否 1是',

    view_num      int          DEFAULT 0 COMMENT '阅读量',
    like_num      int          DEFAULT 0 COMMENT '点赞数',
    comment_num   int          DEFAULT 0 COMMENT '评论数',
    words_num   int          DEFAULT 0 COMMENT '文字数',

    create_time   datetime     DEFAULT NULL COMMENT '创建时间',
    update_time   datetime     DEFAULT NULL COMMENT '修改时间',
    publish_time  datetime     DEFAULT NULL COMMENT '发布时间',
    timed_publish_time datetime DEFAULT NULL  COMMENT '定时发布时间',

    user_id       bigint       DEFAULT NULL COMMENT '作者ID',
    user_nickname varchar(50)  DEFAULT NULL COMMENT '作者昵称',
    user_avatar   varchar(255) DEFAULT NULL COMMENT '作者头像',

    delete_flag   tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    sort          int          DEFAULT 0 COMMENT '排序权重',

    PRIMARY KEY (id),
    KEY idx_status_del_top_pubtime (status, delete_flag, is_top, publish_time),
    KEY idx_category_status_del_pubtime (category_id, status, delete_flag, publish_time),
    KEY idx_user_status_del_pubtime (user_id, status, delete_flag, publish_time),
    KEY idx_status_del_pubtime (status, delete_flag, publish_time),
    KEY idx_status_publishtime (status, publish_time),
    KEY idx_status_hot_del_pubtime (status, is_hot, delete_flag, publish_time),
    KEY idx_timed_publish_time (timed_publish_time)
) COMMENT ='文章表';

-- 文章表_tag
DROP TABLE IF EXISTS article_tag;
CREATE TABLE article_tag
(
    id          bigint unsigned NOT NULL AUTO_INCREMENT,
    article_id  bigint          NOT NULL COMMENT '文章ID',
    tag_id      bigint          NOT NULL COMMENT '标签ID',
    create_time datetime DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_tag (article_id, tag_id)
) COMMENT ='文章标签关联表';

-- 评论表
DROP TABLE IF EXISTS comment;
CREATE TABLE comment
(
    id                  bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    type                tinyint         NOT NULL COMMENT '评论类型 0文章评论 1日常评论 2留言板',
    source_id           bigint          NOT NULL COMMENT '来源ID 文章ID/日常ID/0=留言板',

    msg_type            tinyint      DEFAULT 0 COMMENT '留言类型 0评论留言 1反馈建议 2申请友链',

    parent_id           bigint       DEFAULT 0 COMMENT '父评论ID 0=顶级评论',
    reply_user_id       bigint       DEFAULT 0 COMMENT '回复用户ID 0=没有',
    reply_user_nickname varchar(50)  DEFAULT NULL COMMENT '回复的用户昵称',

    user_id             bigint          NOT NULL COMMENT '评论人ID',
    user_nickname       varchar(50)  DEFAULT NULL COMMENT '评论人昵称',
    user_avatar         varchar(500) DEFAULT NULL COMMENT '评论人头像',

    content             text            NOT NULL COMMENT '评论内容',

    like_num            int          DEFAULT 0 COMMENT '点赞数',
    status              tinyint      DEFAULT 1 COMMENT '状态 0隐藏 1正常',
    is_top              tinyint      DEFAULT 0 COMMENT '是否置顶 0否 1是',

    ip_address          varchar(45)  DEFAULT NULL COMMENT '评论时IP地址',
    browser varchar(100) DEFAULT NULL COMMENT '浏览器',
    os varchar(100) DEFAULT NULL COMMENT '操作系统',
    device_type varchar(20) DEFAULT NULL COMMENT '设备类型',
    location varchar(100) DEFAULT NULL COMMENT '操作地理位置',
    user_agent text DEFAULT NULL COMMENT '原始UA头(浏览器User-Agent)',

    delete_flag         tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    create_time         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (id),
    KEY idx_type_source_del_status_top_time (type, source_id, delete_flag, status, is_top, create_time),
    KEY idx_parent_create_time (parent_id, create_time),
    KEY idx_user_delete_time (user_id, delete_flag, create_time),
    KEY idx_reply_user_id (reply_user_id)
) COMMENT ='公共评论表';

-- 日常表
DROP TABLE IF EXISTS daily;
CREATE TABLE daily
(
    id            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    content       longtext      DEFAULT NULL COMMENT '日常内容',
    images        varchar(1024) DEFAULT NULL COMMENT '图片地址，多个用英文逗号分隔',
    files         varchar(1024) DEFAULT NULL COMMENT '文件地址，多个用英文逗号分隔',

    user_id       bigint          NOT NULL COMMENT '发布者ID',
    user_nickname varchar(50)   DEFAULT NULL COMMENT '作者昵称',
    user_avatar   varchar(255)  DEFAULT NULL COMMENT '作者头像',

    type          tinyint       DEFAULT 0 COMMENT '类型 0纯文字 1图片 2文件 3图文混合',
    is_top        tinyint       DEFAULT 0 COMMENT '是否置顶 0否 1是',
    status        tinyint       DEFAULT 1 COMMENT '状态 0草稿 1已发布 2已下架 3定时发布 4私密',

    delete_flag   tinyint       DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    sort          int           DEFAULT 0 COMMENT '排序权重',
    like_num      int           DEFAULT 0 COMMENT '点赞数',
    comment_num   int           DEFAULT 0 COMMENT '评论数',
    view_num      int           DEFAULT 0 COMMENT '浏览数',

    create_time   datetime      DEFAULT NULL COMMENT '创建时间',
    publish_time  datetime      DEFAULT NULL COMMENT '发布时间',
    timed_publish_time datetime DEFAULT NULL COMMENT '定时发布时间',
    update_time   datetime      DEFAULT NULL COMMENT '修改时间',

    PRIMARY KEY (id),
    KEY idx_status_del_top_pubtime (status, delete_flag, is_top, publish_time),
    KEY idx_user_del_pubtime (user_id, delete_flag, publish_time),
    KEY idx_status_pubtime (status, publish_time),
    KEY idx_timed_publish_time (timed_publish_time)
) COMMENT ='日常记录表';

-- 友链表
DROP TABLE IF EXISTS link;
CREATE TABLE link
(
    id           bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    link_name    varchar(50)     NOT NULL COMMENT '网站名称',
    link_url     varchar(255)    NOT NULL COMMENT '网站链接',
    link_avatar  varchar(500) DEFAULT NULL COMMENT '站点头像图标',
    link_desc    varchar(200) DEFAULT NULL COMMENT '站点简介',
    link_email   varchar(100) DEFAULT NULL COMMENT '申请邮箱',
    sort         int          DEFAULT 0 COMMENT '排序权重',
    audit_status tinyint      DEFAULT 0 COMMENT '审核状态 0待审核 1审核通过 2审核拒绝',
    status       tinyint      DEFAULT 0 COMMENT '展示状态 0禁用 1正常展示',
    delete_flag  tinyint      DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    create_time  datetime     DEFAULT NULL COMMENT '添加时间',
    update_time  datetime     DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_link_url_del (link_url, delete_flag),
    KEY idx_audit_status_del_sort_time (audit_status, status, delete_flag, sort, create_time),
    KEY idx_audit_del_time (audit_status, delete_flag, create_time),
    KEY idx_audit_del_sort_time (audit_status, delete_flag, sort, create_time),
    KEY idx_link_name (link_name)
) COMMENT ='友情链接表';

-- 四、统计、订阅与日志

-- 点赞表
DROP TABLE IF EXISTS user_like;
CREATE TABLE user_like
(
    id            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id       bigint NOT NULL COMMENT '点赞用户ID',
    target_type   tinyint NOT NULL DEFAULT 1 COMMENT '点赞对象类型 0=文章 1=日常 2=评论',
    target_id     bigint NOT NULL COMMENT '点赞目标ID(文章ID/日常ID/评论ID)',
    like_date     date NOT NULL COMMENT '点赞日期',
    delete_flag   tinyint DEFAULT 0 COMMENT '逻辑删除 0=正常 1=取消点赞',
    create_time   datetime DEFAULT NULL COMMENT '点赞时间',
    update_time   datetime DEFAULT NULL COMMENT '更新时间(取消点赞更新)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target_date (user_id, target_type, target_id, like_date, delete_flag),
    -- 快速查询某篇文章所有点赞记录
    KEY idx_target_type_id_del (target_type, target_id, delete_flag),
    -- 查询当前用户所有点赞内容
    KEY idx_user_del (user_id, delete_flag)
) COMMENT ='用户点赞记录表';

-- 文章表_view_record
DROP TABLE IF EXISTS article_view_record;
CREATE TABLE article_view_record
(
    id bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id bigint unsigned NOT NULL COMMENT '文章ID',
    user_id bigint unsigned DEFAULT NULL COMMENT '访客用户ID，未登录为null',
    ip_address varchar(50) NOT NULL COMMENT '访问IP',
    browser varchar(100) DEFAULT NULL COMMENT '浏览器',
    os varchar(100) DEFAULT NULL COMMENT '操作系统',
    device_type varchar(20) DEFAULT NULL COMMENT '设备类型',
    location varchar(100) DEFAULT NULL COMMENT '访问地点',
    user_agent text COMMENT 'UA原始信息',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    view_date date GENERATED ALWAYS AS (DATE(create_time)) STORED COMMENT '浏览日期',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_day_view (article_id, user_id, ip_address, view_date),
    KEY idx_article_time (article_id, create_time)
) COMMENT='文章每日浏览记录表';

-- RSS订阅表
DROP TABLE IF EXISTS rss_subscriptions;
CREATE TABLE rss_subscriptions
(
    id bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    visitor_id bigint unsigned DEFAULT NULL COMMENT '访客ID，未登录订阅时为NULL',
    nickname varchar(30) DEFAULT NULL COMMENT '昵称，未填写时用邮箱前缀',
    email varchar(50) NOT NULL COMMENT '邮箱',
    token              varchar(64)     DEFAULT NULL COMMENT '退订令牌，邮件退订链接使用',
    is_active tinyint NOT NULL DEFAULT 1 COMMENT '是否激活，0-否，1-是',
    subscribe_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    un_subscribe_time datetime DEFAULT NULL COMMENT '取消订阅时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_email (email),
    UNIQUE KEY uk_token (token),
    KEY idx_is_active (is_active),
    KEY idx_visitor_id (visitor_id)
) COMMENT='Rss订阅记录表';

-- 用户设备表
DROP TABLE IF EXISTS user_device;
CREATE TABLE user_device
(
    id              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         bigint unsigned NOT NULL COMMENT '关联用户ID',
    device_uuid     varchar(128)  DEFAULT NULL COMMENT '前端生成设备唯一标识',
    ip_address      varchar(50)   DEFAULT NULL COMMENT '登录公网IP',
    browser         varchar(100)  DEFAULT NULL COMMENT '浏览器名称版本',
    os              varchar(100)  DEFAULT NULL COMMENT '操作系统',
    device_type     varchar(20)   DEFAULT NULL COMMENT '设备类型：电脑/手机/平板/未知',
    location        varchar(100)  DEFAULT NULL COMMENT 'IP解析地理位置(省市)',
    user_agent      text          DEFAULT NULL COMMENT '原始UA请求头',
    is_trust        tinyint NOT NULL DEFAULT 0 COMMENT '是否可信设备 0陌生 1可信',
    last_login_time datetime      DEFAULT NULL COMMENT '最近一次登录时间',
    delete_flag     tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    create_time     datetime      DEFAULT NULL COMMENT '设备首次绑定时间',
    update_time     datetime      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_device_uuid (user_id, device_uuid) COMMENT '同一用户同一设备唯一',
    KEY idx_user_id (user_id)
) COMMENT ='用户登录设备表';

-- 后台操作日志表
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log
(
    id              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志主键ID',
    oper_user_id    bigint unsigned DEFAULT NULL COMMENT '操作人用户ID',
    oper_username   varchar(30)     DEFAULT NULL COMMENT '操作人用户名',
    oper_nickname   varchar(50)     DEFAULT NULL COMMENT '操作人昵称',
    module_name     varchar(50)     DEFAULT NULL COMMENT '操作模块(文章管理/用户管理/角色等)',
    oper_title      varchar(255)    DEFAULT NULL COMMENT '操作标题(新增文章、删除用户等)',
    oper_type       tinyint         DEFAULT NULL COMMENT '操作类型 1新增 2编辑 3删除 4查询 5导出 6导入',
    request_url     varchar(500)    DEFAULT NULL COMMENT '请求接口地址',
    request_method  varchar(20)     DEFAULT NULL COMMENT '请求方式GET/POST/PUT/DELETE',
    ip_address      varchar(50)     DEFAULT NULL COMMENT '操作IP',
    browser         varchar(100)    DEFAULT NULL COMMENT '浏览器',
    os              varchar(100)    DEFAULT NULL COMMENT '操作系统',
    location        varchar(100)    DEFAULT NULL COMMENT '操作地点',
    oper_param      text            DEFAULT NULL COMMENT '请求入参JSON',
    oper_result     text            DEFAULT NULL COMMENT '返回结果JSON',
    cost_time       bigint          DEFAULT NULL COMMENT '接口耗时(ms)',
    status          tinyint NOT NULL DEFAULT 1 COMMENT '操作状态 1成功 0失败',
    error_msg       text            DEFAULT NULL COMMENT '失败异常信息',
    delete_flag     tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    create_time     datetime        DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_time (oper_user_id, create_time),
    KEY idx_create_time (create_time)
) COMMENT ='后台操作日志表';

-- 五、初始化基础数据
-- 添加角色：超级管理员
INSERT INTO sys_role (role_name, role_key, sort, remark,status,delete_flag, create_time, update_time)
VALUES ('超级管理员', 'admin', 1, '拥有系统所有模块全部权限', 1,0,NOW(), NOW());

-- 默认管理员账号：admin / 123456
INSERT INTO sys_user (id, nickname, username, phone, email, sex, password, salt,
                        avatar, intro, status, delete_flag, lock_flag, lock_time,
                        website, github, create_time, update_time, login_ip, login_agent, login_time)
VALUES (1, '管理员', 'admin', NULL, 'admin@example.com', 2,
        'b386d040d10b1c0da4d7e0b288afe283', '3f8a1c52d9b74e6fa0c1d2e3b4a59687',
        NULL, NULL, 1, 0, 0, NULL, NULL, NULL, NOW(), NOW(), NULL, NULL, NULL);

-- 默认管理员绑定超级管理员角色
INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES (1, 1, NOW());

-- 后台菜单与权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, menu_type, icon, sort,status,delete_flag,create_time, update_time)
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
(83,26,'重置密码', NULL, NULL, 'sys:user:reset', 2, NULL, 5, 1,0,NOW(), NOW()),

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

-- ===================== 7、系统设置【顶级目录 parent_id=0】 =====================
(41,0, '系统设置', '/sysSetting', 'Layout', 'sys:setting', 0, 'Menu', 7, 1,0,NOW(), NOW()),
(42,41,'操作日志', '/operLog', 'sysSetting/operlog/index', 'sys:operlog:list', 1, 'Tickets', 2, 1,0,NOW(), NOW()),
(85,42,'删除日志', NULL, NULL, 'sys:operlog:delete', 2, NULL, 1, 1,0,NOW(), NOW()),
(84,41,'首页公告', '/noticeSetting', 'sysSetting/notice/index', 'sys:notice:edit', 1, 'Bell', 1, 1,0,NOW(), NOW()),

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
(54,53,'权限回收站', '/recycleMenu', 'recycleBin/menu/index', 'sys:recycleMenu:list', 1, 'Delete', 1, 1,0,NOW(), NOW()),
(55,53,'角色回收站', '/recycleRole', 'recycleBin/role/index', 'sys:recycleRole:list', 1, 'Delete', 2, 1,0,NOW(), NOW()),
(58,53,'用户回收站', '/recycleUser', 'recycleBin/user/index', 'sys:recycleUser:list', 1, 'Delete', 3, 1,0,NOW(), NOW()),
(56,54,'恢复权限', NULL, NULL, 'sys:recycleMenu:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(57,54,'删除权限', NULL, NULL, 'sys:recycleMenu:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
(59,55,'恢复角色', NULL, NULL, 'sys:recycleRole:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(60,55,'彻底删除角色', NULL, NULL, 'sys:recycleRole:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
(61,58,'恢复用户', NULL, NULL, 'sys:recycleUser:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(62,58,'彻底删除用户', NULL, NULL, 'sys:recycleUser:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 作品回收站（中间层目录，component 留空，不能填 Layout）
(63,53,'作品回收站', '/recycleBin/artwork', NULL, 'sys:recycleArtwork', 0, 'Folder', 4, 1,0,NOW(), NOW()),
-- 文章回收站（页面）
(64,63,'文章回收站', '/recycleBin/artwork/articleWork', 'recycleBin/artwork/articleWork/index', 'sys:recycleArticleWork:list', 1, 'Document', 1, 1,0,NOW(), NOW()),
-- 日常回收站（页面）
(65,63,'日常回收站', '/recycleBin/artwork/dailyWork', 'recycleBin/artwork/dailyWork/index', 'sys:recycleDailyWork:list', 1, 'Memo', 2, 1,0,NOW(), NOW()),
-- 文章回收站按钮权限
(66,64,'恢复文章', NULL, NULL, 'sys:recycleArticleWork:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(67,64,'彻底删除文章', NULL, NULL, 'sys:recycleArticleWork:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 日常回收站按钮权限
(68,65,'恢复日常', NULL, NULL, 'sys:recycleDailyWork:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(69,65,'彻底删除日常', NULL, NULL, 'sys:recycleDailyWork:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 分类标签回收站（页面）
(70,63,'分类标签回收站', '/recycleBin/artwork/categoryOrTag', 'recycleBin/artwork/categoryOrTag/index', 'sys:recycleCategoryTag:list', 1, 'Collection', 3, 1,0,NOW(), NOW()),
(71,70,'恢复分类标签', NULL, NULL, 'sys:recycleCategoryTag:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(72,70,'彻底删除分类标签', NULL, NULL, 'sys:recycleCategoryTag:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
-- 评论回收站（目录，component 留空）
(73,53,'评论回收站', '/recycleBin/comment', NULL, 'sys:recycleComment', 0, 'ChatDotRound', 5, 1,0,NOW(), NOW()),
-- 评论回收站页面
(74,73,'文章评论回收站', '/recycleBin/comment/articleComment', 'recycleBin/comment/articleComment/index', 'sys:recycleArticleComment:list', 1, 'ChatLineSquare', 1, 1,0,NOW(), NOW()),
(75,73,'日常评论回收站', '/recycleBin/comment/dailyComment', 'recycleBin/comment/dailyComment/index', 'sys:recycleDailyComment:list', 1, 'ChatDotRound', 2, 1,0,NOW(), NOW()),
(76,73,'留言评论回收站', '/recycleBin/comment/messageComment', 'recycleBin/comment/messageComment/index', 'sys:recycleMessageComment:list', 1, 'Message', 3, 1,0,NOW(), NOW()),
-- 评论回收站按钮权限
(77,74,'恢复评论', NULL, NULL, 'sys:recycleArticleComment:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(78,74,'彻底删除评论', NULL, NULL, 'sys:recycleArticleComment:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
(79,75,'恢复评论', NULL, NULL, 'sys:recycleDailyComment:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(80,75,'彻底删除评论', NULL, NULL, 'sys:recycleDailyComment:delete', 2, NULL, 2, 1,0,NOW(), NOW()),
(81,76,'恢复评论', NULL, NULL, 'sys:recycleMessageComment:recycle', 2, NULL, 1, 1,0,NOW(), NOW()),
(82,76,'彻底删除评论', NULL, NULL, 'sys:recycleMessageComment:delete', 2, NULL, 2, 1,0,NOW(), NOW())
;

-- 角色与菜单权限绑定
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
SELECT 1, sm.id, NOW()
FROM sys_menu sm
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu srm
    WHERE srm.role_id = 1 AND srm.menu_id = sm.id
);

-- 站点配置占位项
-- 网站正式运行起始时间
INSERT IGNORE INTO system_config (config_key, config_value, config_type, description, create_time, update_time)
VALUES ('site.launch_time', DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 'date', '网站正式运行起始时间', NOW(), NOW());
-- icp备案信息
INSERT IGNORE INTO system_config (config_key, config_value, config_type, description, create_time, update_time)
VALUES ('icp-beian', 'xxx', 'string', 'ICP备案号，按需填写', NOW(), NOW());
-- 公安备案信息
INSERT IGNORE INTO system_config (config_key, config_value, config_type, description, create_time, update_time)
VALUES ('gongan-beian', 'xxx', 'string', '公安备案号，按需填写', NOW(), NOW());
