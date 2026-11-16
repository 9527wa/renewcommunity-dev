-- RenewCommunity 业务模块初始化脚本：项目、投诉、资金
-- 执行方式：mysql -uroot -p renew_community < sql/business.sql

USE renew_community;

CREATE TABLE IF NOT EXISTS sys_project (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    name        VARCHAR(100)  NOT NULL COMMENT '项目名称',
    community   VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '所属小区',
    category    VARCHAR(50)   NOT NULL DEFAULT '' COMMENT '改造类别',
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0规划中 1施工中 2已完成',
    progress    INT           NOT NULL DEFAULT 0 COMMENT '进度百分比 0-100',
    budget      DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '预算金额（元）',
    start_date  DATE                   DEFAULT NULL COMMENT '计划开始日期',
    end_date    DATE                   DEFAULT NULL COMMENT '计划结束日期',
    manager     VARCHAR(50)   NOT NULL DEFAULT '' COMMENT '项目负责人',
    description VARCHAR(500)  NOT NULL DEFAULT '' COMMENT '项目简介',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_project_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '改造项目';

CREATE TABLE IF NOT EXISTS sys_complaint (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
    title          VARCHAR(100) NOT NULL COMMENT '投诉标题',
    content        VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '投诉内容',
    community      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '所属小区',
    type           VARCHAR(30)  NOT NULL DEFAULT '其他' COMMENT '投诉类型',
    status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1处理中 2已办结',
    project_id     BIGINT                DEFAULT NULL COMMENT '关联项目ID',
    submitter_id   BIGINT                DEFAULT NULL COMMENT '提交人ID（C端用户）',
    contact_name   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '联系人',
    contact_mobile VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '联系电话',
    reply          VARCHAR(500) NOT NULL DEFAULT '' COMMENT '处理回复',
    handled_at     DATETIME              DEFAULT NULL COMMENT '处理时间',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_complaint_status (status),
    KEY idx_sys_complaint_project_id (project_id),
    KEY idx_sys_complaint_submitter (submitter_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '投诉建议';

CREATE TABLE IF NOT EXISTS sys_fund (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '资金ID',
    project_id  BIGINT                 DEFAULT NULL COMMENT '关联项目ID',
    name        VARCHAR(100)  NOT NULL COMMENT '资金名称',
    type        TINYINT       NOT NULL COMMENT '类型：1拨款 2支出',
    amount      DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '金额（元）',
    occurred_at DATE          NOT NULL COMMENT '发生日期',
    remark      VARCHAR(200)  NOT NULL DEFAULT '' COMMENT '备注',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_fund_type (type),
    KEY idx_sys_fund_project_id (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '资金台账';

-- 示例项目
INSERT IGNORE INTO sys_project (id, name, community, category, status, progress, budget, start_date, end_date, manager, description) VALUES
    (1, '幸福家园道路及管网改造', '幸福家园小区', '市政管网', 1, 65, 12800000.00, '2026-11-01', '2027-02-28', '张伟', '对小区内部道路及雨污管网进行整体改造'),
    (2, '幸福家园外墙保温改造', '幸福家园小区', '建筑节能', 1, 40, 8600000.00, '2026-10-15', '2027-01-31', '李娜', '对 12 栋住宅外墙实施保温与节能改造'),
    (3, '幸福家园雨污分流工程', '幸福家园小区', '市政管网', 0, 0, 5600000.00, '2026-12-01', '2027-03-31', '王强', '新建雨污分流管网，解决汛期积水问题'),
    (4, '幸福家园电梯加装一期', '幸福家园小区', '适老化改造', 1, 82, 4200000.00, '2026-08-01', '2026-12-31', '赵敏', '为 8 个单元加装电梯'),
    (5, '幸福家园绿化提升', '幸福家园小区', '环境整治', 2, 100, 1800000.00, '2026-06-01', '2026-10-31', '陈超', '新增口袋公园与健身步道'),
    (6, '阳光花园停车位改造', '阳光花园小区', '停车设施', 0, 0, 2300000.00, '2027-03-01', '2027-06-30', '刘洋', '扩建地面停车位并新增充电桩'),
    (7, '幸福家园智慧安防', '幸福家园小区', '安防设施', 2, 100, 3100000.00, '2026-05-01', '2026-09-30', '张伟', '智慧门禁、视频监控与消防预警');

-- 示例投诉
INSERT IGNORE INTO sys_complaint (id, title, content, community, type, status, project_id, contact_name, contact_mobile, reply, handled_at, created_at) VALUES
    (1, '施工噪音影响休息', '中午休息时间仍有机械施工，噪音较大，希望调整作业时间。', '幸福家园小区', '施工扰民', 0, 1, '王女士', '13800001111', '', NULL, '2026-11-12 13:20:00'),
    (2, '楼道照明损坏', '3 号楼 2 单元楼道灯不亮，晚上上下楼很不安全。', '幸福家园小区', '物业维修', 1, NULL, '李先生', '13800002222', '已联系物业检修', '2026-11-10 09:30:00', '2026-11-09 20:15:00'),
    (3, '施工围挡占道过多', '道路改造围挡占用了一半车道，早晚高峰通行困难。', '幸福家园小区', '施工扰民', 1, 1, '张先生', '13800003333', '已协调施工单位优化围挡', '2026-11-08 16:40:00', '2026-11-07 08:50:00'),
    (4, '绿化带垃圾未清理', '小区东侧绿化带内有建筑垃圾，长期无人清理。', '幸福家园小区', '环境秩序', 2, 5, '陈女士', '13800004444', '垃圾已清运，绿化带已恢复', '2026-11-04 10:00:00', '2026-11-03 09:10:00'),
    (5, '电梯加装公示时间太短', '电梯加装公示只有三天，很多业主没看到，希望延长公示。', '幸福家园小区', '其他', 2, 4, '赵先生', '13800005555', '公示已延长至 7 天', '2026-10-28 15:20:00', '2026-10-26 11:30:00'),
    (6, '施工现场扬尘较大', '道路施工未及时洒水，家里窗户不能开。', '幸福家园小区', '施工扰民', 0, 1, '刘女士', '13800006666', '', NULL, '2026-10-22 14:05:00'),
    (7, '停车位不足', '小区车辆增多，现有停车位严重不足，希望尽快改造。', '阳光花园小区', '其他', 1, 6, '周先生', '13800007777', '已纳入停车位改造计划', '2026-10-18 09:00:00', '2026-10-16 19:40:00'),
    (8, '消防通道被占用', '夜间有车辆停在消防通道，存在安全隐患。', '幸福家园小区', '安全隐患', 2, 7, '吴女士', '13800008888', '已清理并加装禁停标识', '2026-10-12 17:30:00', '2026-10-12 08:20:00');

-- 示例资金台账
INSERT IGNORE INTO sys_fund (id, project_id, name, type, amount, occurred_at, remark) VALUES
    (1, 1, '道路及管网改造首期拨款', 1, 5000000.00, '2026-11-02', '区财政首批拨付'),
    (2, 1, '道路及管网改造施工款', 2, 2600000.00, '2026-11-10', '支付施工单位进度款'),
    (3, 2, '外墙保温改造首期拨款', 1, 3500000.00, '2026-10-18', '区财政拨付'),
    (4, 2, '外墙保温材料采购', 2, 1200000.00, '2026-10-25', '保温材料采购款'),
    (5, 4, '电梯加装专项拨款', 1, 3000000.00, '2026-08-05', '专项补助资金'),
    (6, 4, '电梯设备采购与安装', 2, 2100000.00, '2026-09-20', '设备采购及安装费用'),
    (7, 5, '绿化提升完工结算', 2, 1650000.00, '2026-11-01', '工程完工结算款'),
    (8, 7, '智慧安防项目支出', 2, 2800000.00, '2026-09-28', '设备与平台建设'),
    (9, 3, '雨污分流工程前期经费', 1, 1000000.00, '2026-11-05', '前期勘察设计费'),
    (10, 6, '停车位改造前期经费', 1, 800000.00, '2026-11-08', '阳光花园停车位改造前期费用');

-- ===== 小区管理 =====
CREATE TABLE IF NOT EXISTS sys_community (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '小区ID',
    name            VARCHAR(100) NOT NULL COMMENT '小区名称',
    address         VARCHAR(200) NOT NULL DEFAULT '' COMMENT '小区地址',
    built_year      INT                   DEFAULT NULL COMMENT '建成年份',
    building_count  INT          NOT NULL DEFAULT 0 COMMENT '楼栋数',
    household_count INT          NOT NULL DEFAULT 0 COMMENT '户数',
    manager         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '负责人',
    manager_mobile  VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '负责人电话',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    remark          VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_community_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '小区信息';

INSERT IGNORE INTO sys_community (id, name, address, built_year, building_count, household_count, manager, manager_mobile, status, remark) VALUES
    (1, '幸福家园小区', '幸福路 88 号', 2003, 18, 1260, '张伟', '13800001111', 1, '2026 年老旧小区改造重点小区'),
    (2, '阳光花园小区', '阳光路 66 号', 2008, 12, 860, '刘洋', '13800002222', 1, '停车位改造与绿化提升'),
    (3, '和平里小区', '和平街 12 号', 1998, 9, 520, '王强', '13800003333', 1, '待纳入下一批改造计划');

-- ===== 公告管理 =====
CREATE TABLE IF NOT EXISTS sys_announcement (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title        VARCHAR(200) NOT NULL COMMENT '公告标题',
    content      TEXT         NOT NULL COMMENT '公告内容',
    type         VARCHAR(30)  NOT NULL DEFAULT '公告通知' COMMENT '公告类型',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已下架',
    published_at DATETIME              DEFAULT NULL COMMENT '发布时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_announcement_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告';

INSERT IGNORE INTO sys_announcement (id, title, content, type, status, published_at, created_at) VALUES
    (1, '道路升级施工公告', '幸福家园小区道路及管网改造正在推进中，请居民合理安排出行，注意施工安全。施工期间部分道路将临时封闭，请提前规划出行路线。', '施工公告', 1, '2026-11-12 09:00:00', '2026-11-12 08:30:00'),
    (2, '关于改造期间临时停车调整的通知', '为配合道路施工，自 1 月 22 日起小区部分区域停车位将临时调整，具体位置见现场指引。', '通知公告', 0, NULL, '2026-11-10 15:20:00'),
    (3, '电梯加装公示', '幸福家园小区电梯加装一期方案已公示，公示期 7 天，如有意见请向社区反馈。', '公示公告', 2, '2026-10-20 10:00:00', '2026-10-18 16:00:00');

-- ===== 文件与资料台账 =====
CREATE TABLE IF NOT EXISTS sys_file (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    stored_name   VARCHAR(200) NOT NULL COMMENT '存储文件名',
    content_type  VARCHAR(100) NOT NULL DEFAULT '' COMMENT '文件类型',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    uploader_id   BIGINT                DEFAULT NULL COMMENT '上传人ID',
    uploader_name VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '上传人',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件存储记录';

CREATE TABLE IF NOT EXISTS sys_document (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资料ID',
    project_id  BIGINT                DEFAULT NULL COMMENT '关联项目ID',
    category    VARCHAR(30)  NOT NULL DEFAULT '其他' COMMENT '资料分类',
    title       VARCHAR(200) NOT NULL COMMENT '资料名称',
    file_id     BIGINT       NOT NULL COMMENT '文件ID',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0正常 1已归档',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_document_project_id (project_id),
    KEY idx_sys_document_category (category),
    KEY idx_sys_document_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '资料台账';

-- ===== 工程进度 =====
CREATE TABLE IF NOT EXISTS sys_progress (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '进度ID',
    project_id   BIGINT        NOT NULL COMMENT '项目ID',
    stage        VARCHAR(20)   NOT NULL DEFAULT '施工阶段' COMMENT '阶段：准备阶段/施工阶段/验收阶段',
    title        VARCHAR(200)  NOT NULL COMMENT '进度标题',
    content      VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '进度说明',
    progress     INT           NOT NULL DEFAULT 0 COMMENT '更新后的项目进度',
    report_date  DATE          NOT NULL COMMENT '上报日期',
    creator_id   BIGINT                 DEFAULT NULL COMMENT '上报人ID',
    creator_name VARCHAR(50)   NOT NULL DEFAULT '' COMMENT '上报人',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_progress_project_id (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工程进度记录';

CREATE TABLE IF NOT EXISTS sys_progress_image (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    progress_id BIGINT NOT NULL COMMENT '进度ID',
    file_id     BIGINT NOT NULL COMMENT '文件ID',
    sort        INT    NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    KEY idx_sys_progress_image_progress_id (progress_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工程进度现场图片';

-- ===== 项目验收 =====
CREATE TABLE IF NOT EXISTS sys_acceptance (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '验收ID',
    project_id   BIGINT        NOT NULL COMMENT '项目ID',
    title        VARCHAR(200)  NOT NULL COMMENT '验收名称',
    result       TINYINT       NOT NULL DEFAULT 0 COMMENT '结果：0待验收 1通过 2不通过',
    accept_date  DATE                   DEFAULT NULL COMMENT '验收日期',
    conclusion   VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '验收结论',
    creator_id   BIGINT                 DEFAULT NULL COMMENT '登记人ID',
    creator_name VARCHAR(50)   NOT NULL DEFAULT '' COMMENT '登记人',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_acceptance_project_id (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '项目验收';

CREATE TABLE IF NOT EXISTS sys_acceptance_file (
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    acceptance_id BIGINT NOT NULL COMMENT '验收ID',
    file_id       BIGINT NOT NULL COMMENT '文件ID',
    sort          INT    NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    KEY idx_sys_acceptance_file_acceptance_id (acceptance_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '项目验收附件';

INSERT IGNORE INTO sys_progress (id, project_id, stage, title, content, progress, report_date, creator_id, creator_name) VALUES
    (1, 1, '施工阶段', '道路基层处理完成', '道路基层已全部处理完成，正在进行管网铺设与路面恢复准备。', 65, '2026-11-12', 1, '管理员'),
    (2, 1, '施工阶段', '管网铺设进度更新', '雨污管网铺设完成约 60%，预计本周完成主干管施工。', 58, '2026-11-08', 1, '管理员'),
    (3, 2, '准备阶段', '施工材料进场', '外墙保温材料已完成首批进场验收，施工队伍进场准备中。', 40, '2026-11-05', 1, '管理员'),
    (4, 4, '施工阶段', '电梯设备安装', '8 个单元电梯设备已完成安装，进入调试阶段。', 82, '2026-11-10', 1, '管理员');

INSERT IGNORE INTO sys_acceptance (id, project_id, title, result, accept_date, conclusion, creator_id, creator_name) VALUES
    (1, 5, '绿化提升工程竣工验收', 1, '2026-10-28', '绿化提升工程验收合格，口袋公园与健身步道已投入使用。', 1, '管理员'),
    (2, 7, '智慧安防项目验收', 1, '2026-09-25', '门禁、视频监控与消防预警系统运行正常，验收通过。', 1, '管理员'),
    (3, 4, '电梯加装一期阶段验收', 0, NULL, '待电梯调试完成后组织验收。', 1, '管理员');

-- ===== 居民绑定小区（sys_user.community_id） =====
SET @has_community := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'community_id');
SET @alter_community := IF(@has_community = 0,
    'ALTER TABLE sys_user ADD COLUMN community_id BIGINT NULL COMMENT ''绑定小区ID'' AFTER email', 'SELECT 1');
PREPARE stmt_community FROM @alter_community;
EXECUTE stmt_community;
DEALLOCATE PREPARE stmt_community;

CREATE TABLE IF NOT EXISTS sys_user_community_log (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT      NOT NULL COMMENT '用户ID',
    community_id  BIGINT      NOT NULL COMMENT '小区ID',
    operator_id   BIGINT               DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    PRIMARY KEY (id),
    KEY idx_sys_user_community_log_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '居民小区绑定记录';

-- 示例居民账号（密码均为 123456）
INSERT IGNORE INTO sys_user (id, username, password, nickname, mobile, community_id, status) VALUES
    (2, 'resident_li', '$2y$10$JvfGnak/56TzuCA0LY0CieCOUAHEDQynxdvHYc9RUK3sf3sJrb6yK', '李居民', '13911112222', 1, 1),
    (3, 'resident_wang', '$2y$10$JvfGnak/56TzuCA0LY0CieCOUAHEDQynxdvHYc9RUK3sf3sJrb6yK', '王居民', '13933334444', 2, 1);
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (2, 3), (3, 3);
INSERT IGNORE INTO sys_user_community_log (id, user_id, community_id, operator_id, operator_name) VALUES
    (1, 2, 1, 1, '管理员'),
    (2, 3, 2, 1, '管理员');

-- ===== 满意度评价 =====
CREATE TABLE IF NOT EXISTS sys_satisfaction (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    complaint_id BIGINT       NOT NULL COMMENT '投诉ID',
    user_id      BIGINT       NOT NULL COMMENT '居民用户ID',
    user_name    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '居民名称',
    score        TINYINT      NOT NULL COMMENT '评分 1-5',
    comment      VARCHAR(300) NOT NULL DEFAULT '' COMMENT '评价内容',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_satisfaction_complaint (complaint_id),
    KEY idx_sys_satisfaction_score (score)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '满意度评价';

INSERT IGNORE INTO sys_satisfaction (id, complaint_id, user_id, user_name, score, comment, created_at) VALUES
    (1, 4, 2, '李居民', 5, '垃圾清理很及时，工作人员态度很好。', '2026-11-05 10:20:00'),
    (2, 5, 3, '王居民', 4, '公示时间延长了，处理比较满意。', '2026-10-29 16:30:00'),
    (3, 8, 2, '李居民', 5, '消防通道清理及时，安全隐患消除了。', '2026-10-13 09:10:00');

-- ===== 操作日志 =====
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id     BIGINT                DEFAULT NULL COMMENT '操作人ID',
    user_name   VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '操作人',
    method      VARCHAR(10)  NOT NULL DEFAULT '' COMMENT '请求方法',
    path        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '请求路径',
    ip          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'IP',
    duration_ms BIGINT       NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
    success     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否成功：1成功 0失败',
    error_msg   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '失败信息',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_sys_operation_log_created_at (created_at),
    KEY idx_sys_operation_log_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志';

-- ===== C 端完善：施工单位、投诉事项类型、投诉图片 =====
SET @has_unit := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_project' AND COLUMN_NAME = 'construction_unit');
SET @alter_unit := IF(@has_unit = 0,
    'ALTER TABLE sys_project ADD COLUMN construction_unit VARCHAR(100) NOT NULL DEFAULT '''' COMMENT ''施工单位'' AFTER manager',
    'SELECT 1');
PREPARE stmt_unit FROM @alter_unit;
EXECUTE stmt_unit;
DEALLOCATE PREPARE stmt_unit;

SET @has_kind := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_complaint' AND COLUMN_NAME = 'kind');
SET @alter_kind := IF(@has_kind = 0,
    'ALTER TABLE sys_complaint ADD COLUMN kind VARCHAR(20) NOT NULL DEFAULT ''投诉'' COMMENT ''事项类型：投诉/建议/咨询'' AFTER type',
    'SELECT 1');
PREPARE stmt_kind FROM @alter_kind;
EXECUTE stmt_kind;
DEALLOCATE PREPARE stmt_kind;

CREATE TABLE IF NOT EXISTS sys_complaint_image (
    id           BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    complaint_id BIGINT NOT NULL COMMENT '投诉ID',
    file_id      BIGINT NOT NULL COMMENT '文件ID',
    sort         INT    NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    KEY idx_sys_complaint_image_complaint_id (complaint_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '投诉图片';

UPDATE sys_project SET construction_unit = '市政建设集团' WHERE id IN (1, 3);
UPDATE sys_project SET construction_unit = '城建安装工程公司' WHERE id IN (2, 4);
UPDATE sys_project SET construction_unit = '园林绿化工程公司' WHERE id = 5;
UPDATE sys_project SET construction_unit = '智慧城市科技公司' WHERE id = 7;

-- ===== C 端原型补齐字段 =====
SET @has_location := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_complaint' AND COLUMN_NAME = 'location');
SET @alter_location := IF(@has_location = 0,
    'ALTER TABLE sys_complaint ADD COLUMN location VARCHAR(200) NOT NULL DEFAULT '''' COMMENT ''问题位置'' AFTER community',
    'SELECT 1');
PREPARE stmt_location FROM @alter_location;
EXECUTE stmt_location;
DEALLOCATE PREPARE stmt_location;

SET @has_remind := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_complaint' AND COLUMN_NAME = 'remind_count');
SET @alter_remind := IF(@has_remind = 0,
    'ALTER TABLE sys_complaint ADD COLUMN remind_count INT NOT NULL DEFAULT 0 COMMENT ''催办次数'' AFTER status',
    'SELECT 1');
PREPARE stmt_remind FROM @alter_remind;
EXECUTE stmt_remind;
DEALLOCATE PREPARE stmt_remind;

SET @has_last_remind := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_complaint' AND COLUMN_NAME = 'last_remind_at');
SET @alter_last_remind := IF(@has_last_remind = 0,
    'ALTER TABLE sys_complaint ADD COLUMN last_remind_at DATETIME NULL COMMENT ''最近催办时间'' AFTER remind_count',
    'SELECT 1');
PREPARE stmt_last_remind FROM @alter_last_remind;
EXECUTE stmt_last_remind;
DEALLOCATE PREPARE stmt_last_remind;
