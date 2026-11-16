-- RenewCommunity RBAC 初始化脚本
-- 执行方式：mysql -uroot -proot < sql/rbac.sql

CREATE DATABASE IF NOT EXISTS renew_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE renew_community;

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    nickname    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    mobile      VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    community_id BIGINT                DEFAULT NULL COMMENT '绑定小区ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    code        VARCHAR(50)  NOT NULL COMMENT '角色标识',
    name        VARCHAR(50)  NOT NULL COMMENT '角色名称',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    remark      VARCHAR(200)          DEFAULT NULL COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统角色';

CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID，0 为顶级',
    name        VARCHAR(50)  NOT NULL COMMENT '名称',
    type        CHAR(1)      NOT NULL COMMENT '类型：M目录 C菜单 B按钮',
    path        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '路由路径',
    component   VARCHAR(200) NOT NULL DEFAULT '' COMMENT '组件路径',
    permission  VARCHAR(100) NOT NULL DEFAULT '' COMMENT '权限标识',
    icon        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_menu_parent_id (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '菜单与按钮权限';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色关联';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu_id (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色菜单关联';

-- 初始化角色
INSERT IGNORE INTO sys_role (id, code, name, sort, status, remark) VALUES
    (1, 'R_SUPER', '超级管理员', 1, 1, '拥有全部菜单与权限'),
    (2, 'R_ADMIN', '项目管理员', 2, 1, '负责项目资料台账维护'),
    (3, 'R_USER', '普通用户', 3, 1, '小程序注册用户默认角色');

-- 初始化菜单与按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort) VALUES
    (1, 0, '系统管理', 'M', '/system', '', '', 'mdi:cog', 10),
    (2, 1, '用户管理', 'C', '/system/user', 'system/user', 'system:user:list', 'mdi:account', 1),
    (3, 1, '角色管理', 'C', '/system/role', 'system/role', 'system:role:list', 'mdi:account-group', 2),
    (4, 1, '菜单管理', 'C', '/system/menu', 'system/menu', 'system:menu:list', 'mdi:menu', 3),
    (5, 1, '服务器监控', 'C', '/system/monitor', 'system/monitor', 'system:monitor:view', 'mdi:server', 4),
    (10, 2, '新增用户', 'B', '', '', 'system:user:add', '', 1),
    (11, 2, '修改用户', 'B', '', '', 'system:user:edit', '', 2),
    (12, 2, '删除用户', 'B', '', '', 'system:user:delete', '', 3),
    (13, 2, '重置密码', 'B', '', '', 'system:user:reset-password', '', 4),
    (20, 3, '新增角色', 'B', '', '', 'system:role:add', '', 1),
    (21, 3, '修改角色', 'B', '', '', 'system:role:edit', '', 2),
    (22, 3, '删除角色', 'B', '', '', 'system:role:delete', '', 3),
    (23, 3, '分配权限', 'B', '', '', 'system:role:assign', '', 4),
    (30, 4, '新增菜单', 'B', '', '', 'system:menu:add', '', 1),
    (31, 4, '修改菜单', 'B', '', '', 'system:menu:edit', '', 2),
    (32, 4, '删除菜单', 'B', '', '', 'system:menu:delete', '', 3),
    (40, 0, '项目管理', 'C', '/project', 'project', 'project:list', 'mdi:office-building', 20),
    (50, 40, '新增项目', 'B', '', '', 'project:add', '', 1),
    (51, 40, '修改项目', 'B', '', '', 'project:edit', '', 2),
    (52, 40, '删除项目', 'B', '', '', 'project:delete', '', 3),
    (41, 0, '投诉管理', 'C', '/complaint', 'complaint', 'complaint:list', 'mdi:message-alert', 21),
    (60, 41, '新增投诉', 'B', '', '', 'complaint:add', '', 1),
    (61, 41, '修改投诉', 'B', '', '', 'complaint:edit', '', 2),
    (62, 41, '删除投诉', 'B', '', '', 'complaint:delete', '', 3),
    (63, 41, '处理投诉', 'B', '', '', 'complaint:handle', '', 4),
    (42, 0, '资金管理', 'C', '/fund', 'fund', 'fund:list', 'mdi:cash-multiple', 22),
    (70, 42, '新增资金', 'B', '', '', 'fund:add', '', 1),
    (71, 42, '修改资金', 'B', '', '', 'fund:edit', '', 2),
    (72, 42, '删除资金', 'B', '', '', 'fund:delete', '', 3),
    (43, 0, '小区管理', 'C', '/community', 'community', 'community:list', 'mdi:home-city', 23),
    (80, 43, '新增小区', 'B', '', '', 'community:add', '', 1),
    (81, 43, '修改小区', 'B', '', '', 'community:edit', '', 2),
    (82, 43, '删除小区', 'B', '', '', 'community:delete', '', 3),
    (44, 0, '公告管理', 'C', '/announcement', 'announcement', 'announcement:list', 'mdi:bullhorn', 24),
    (90, 44, '新增公告', 'B', '', '', 'announcement:add', '', 1),
    (91, 44, '修改公告', 'B', '', '', 'announcement:edit', '', 2),
    (92, 44, '删除公告', 'B', '', '', 'announcement:delete', '', 3),
    (45, 0, '资料台账', 'C', '/document', 'document', 'document:list', 'mdi:folder-text', 25),
    (100, 45, '上传资料', 'B', '', '', 'document:upload', '', 1),
    (101, 45, '下载资料', 'B', '', '', 'document:download', '', 2),
    (102, 45, '归档资料', 'B', '', '', 'document:archive', '', 3),
    (103, 45, '删除资料', 'B', '', '', 'document:delete', '', 4),
    (104, 45, '下载文件', 'B', '', '', 'file:download', '', 5),
    (46, 0, '工程进度', 'C', '/progress', 'progress', 'progress:list', 'mdi:chart-timeline-variant', 26),
    (110, 46, '上报进度', 'B', '', '', 'progress:add', '', 1),
    (111, 46, '修改进度', 'B', '', '', 'progress:edit', '', 2),
    (112, 46, '删除进度', 'B', '', '', 'progress:delete', '', 3),
    (47, 0, '项目验收', 'C', '/acceptance', 'acceptance', 'acceptance:list', 'mdi:clipboard-check', 27),
    (120, 47, '登记验收', 'B', '', '', 'acceptance:add', '', 1),
    (121, 47, '修改验收', 'B', '', '', 'acceptance:edit', '', 2),
    (122, 47, '删除验收', 'B', '', '', 'acceptance:delete', '', 3),
    (48, 0, '满意度管理', 'C', '/satisfaction', 'satisfaction', 'satisfaction:list', 'mdi:emoticon-happy-outline', 28),
    (49, 0, '居民用户', 'C', '/resident', 'resident', 'resident:list', 'mdi:account-multiple', 29),
    (130, 49, '绑定小区', 'B', '', '', 'resident:bind', '', 1),
    (53, 0, '统计分析', 'C', '/statistics', 'statistics', 'statistics:view', 'mdi:chart-box-outline', 30),
    (54, 0, '文件管理', 'C', '/file', 'file', 'file:list', 'mdi:folder-multiple', 31),
    (140, 54, '删除文件', 'B', '', '', 'file:delete', '', 1),
    (55, 0, '操作日志', 'C', '/log', 'log', 'log:list', 'mdi:history', 32);

-- 初始化管理员账号：admin / 123456
INSERT IGNORE INTO sys_user (id, username, password, nickname, status) VALUES
    (1, 'admin', '$2y$10$JvfGnak/56TzuCA0LY0CieCOUAHEDQynxdvHYc9RUK3sf3sJrb6yK', '管理员', 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 超级管理员默认拥有全部菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
