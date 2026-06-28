-- ============================================================
-- 拖延症杀手 (Procrastination Killer) 数据库初始化脚本
-- 数据库: MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS procrastination_killer
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE procrastination_killer;

-- ============================================================
-- 1. 用户表 (user)
-- 存储注册用户的基本信息，密码使用 BCrypt 加密存储
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID（主键）',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名（登录用）',
    `password`      VARCHAR(255)    NOT NULL                 COMMENT '密码（BCrypt加密）',
    `email`         VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `nickname`      VARCHAR(50)     DEFAULT NULL             COMMENT '昵称',
    `avatar_url`    VARCHAR(500)    DEFAULT NULL             COMMENT '头像URL',
    `enabled`       TINYINT(1)      NOT NULL DEFAULT 1       COMMENT '账号启用状态（1-启用 0-禁用）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 目标表 (goal)
-- 存储用户设定的宏观目标，关联用户ID实现数据隔离
-- ============================================================
CREATE TABLE IF NOT EXISTS `goal` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '目标ID（主键）',
    `user_id`       BIGINT          NOT NULL                 COMMENT '所属用户ID',
    `title`         VARCHAR(200)    NOT NULL                 COMMENT '目标标题',
    `description`   TEXT            DEFAULT NULL             COMMENT '目标描述（AI生成的详细说明）',
    `status`        VARCHAR(20)     NOT NULL DEFAULT '进行中' COMMENT '目标状态（进行中 / 已完成）',
    `progress`      INT             NOT NULL DEFAULT 0       COMMENT '完成进度（0-100）',
    `task_total`    INT             NOT NULL DEFAULT 0       COMMENT '任务总数',
    `task_completed` INT            NOT NULL DEFAULT 0       COMMENT '已完成任务数',
    `ai_raw_response` TEXT          DEFAULT NULL             COMMENT 'AI原始返回（调试/回溯用）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_goal_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标表（父表）';

-- ============================================================
-- 3. 任务表 (task)
-- 存储AI拆分后的具体小任务，关联目标ID
-- ============================================================
CREATE TABLE IF NOT EXISTS `task` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '任务ID（主键）',
    `goal_id`       BIGINT          NOT NULL                 COMMENT '所属目标ID',
    `content`       VARCHAR(500)    NOT NULL                 COMMENT '任务内容',
    `status`        VARCHAR(20)     NOT NULL DEFAULT '待办'  COMMENT '任务状态（待办 / 已完成）',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序序号（AI拆分时的建议执行顺序）',
    `completed_at`  DATETIME        DEFAULT NULL             COMMENT '完成时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_goal_id` (`goal_id`),
    KEY `idx_goal_status` (`goal_id`, `status`),
    KEY `idx_sort_order` (`goal_id`, `sort_order`),
    CONSTRAINT `fk_task_goal` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表（子表）';
