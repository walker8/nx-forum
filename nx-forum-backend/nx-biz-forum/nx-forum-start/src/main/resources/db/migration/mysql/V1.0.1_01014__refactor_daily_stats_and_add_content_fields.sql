-- =====================================================
-- 重构每日数据统计表：为内容表添加终端类型和平台字段
-- =====================================================

-- =====================================================
-- 步骤1: 为 bbs_threads 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE `bbs_threads`
ADD COLUMN `terminal_type` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）' AFTER `user_agent`,
ADD COLUMN `platform` varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）' AFTER `terminal_type`;

-- 添加索引以提高查询性能
ALTER TABLE `bbs_threads`
ADD INDEX `idx_create_time_terminal` (`create_time`, `terminal_type`) USING BTREE,
ADD INDEX `idx_create_time_platform` (`create_time`, `platform`) USING BTREE,
ADD INDEX `idx_create_time_terminal_platform` (`create_time`, `terminal_type`, `platform`) USING BTREE;

-- =====================================================
-- 步骤2: 为 bbs_comments 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE `bbs_comments`
ADD COLUMN `terminal_type` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）' AFTER `user_agent`,
ADD COLUMN `platform` varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）' AFTER `terminal_type`;

-- 添加索引
ALTER TABLE `bbs_comments`
ADD INDEX `idx_create_time_terminal` (`create_time`, `terminal_type`) USING BTREE,
ADD INDEX `idx_create_time_platform` (`create_time`, `platform`) USING BTREE,
ADD INDEX `idx_create_time_terminal_platform` (`create_time`, `terminal_type`, `platform`) USING BTREE;

-- =====================================================
-- 步骤3: 为 bbs_comment_replies 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE `bbs_comment_replies`
ADD COLUMN `terminal_type` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）' AFTER `user_agent`,
ADD COLUMN `platform` varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）' AFTER `terminal_type`;

-- 添加索引
ALTER TABLE `bbs_comment_replies`
ADD INDEX `idx_create_time_terminal` (`create_time`, `terminal_type`) USING BTREE,
ADD INDEX `idx_create_time_platform` (`create_time`, `platform`) USING BTREE,
ADD INDEX `idx_create_time_terminal_platform` (`create_time`, `terminal_type`, `platform`) USING BTREE;

-- =====================================================
-- 步骤4: 数据修复脚本 - 回填 bbs_threads
-- =====================================================
UPDATE `bbs_threads` t
SET
    t.`terminal_type` = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN t.`user_agent` LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN t.`user_agent` LIKE '%Android%' OR t.`user_agent` LIKE '%iPhone%' OR
             t.`user_agent` LIKE '%iPad%' OR t.`user_agent` LIKE '%Mobile%' OR
             t.`user_agent` LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    t.`platform` = CASE
        WHEN t.`user_agent` LIKE '%Windows%' THEN 'Windows'
        WHEN t.`user_agent` LIKE '%Mac%' THEN 'Mac'
        WHEN t.`user_agent` LIKE '%Linux%' THEN 'Linux'
        WHEN t.`user_agent` LIKE '%Android%' THEN 'Android'
        WHEN t.`user_agent` LIKE '%iPhone%' OR t.`user_agent` LIKE '%iPad%' OR t.`user_agent` LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE t.`user_agent` IS NOT NULL AND t.`user_agent` != '';

-- =====================================================
-- 步骤5: 数据修复脚本 - 回填 bbs_comments
-- =====================================================
UPDATE `bbs_comments` c
SET
    c.`terminal_type` = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN c.`user_agent` LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN c.`user_agent` LIKE '%Android%' OR c.`user_agent` LIKE '%iPhone%' OR
             c.`user_agent` LIKE '%iPad%' OR c.`user_agent` LIKE '%Mobile%' OR
             c.`user_agent` LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    c.`platform` = CASE
        WHEN c.`user_agent` LIKE '%Windows%' THEN 'Windows'
        WHEN c.`user_agent` LIKE '%Mac%' THEN 'Mac'
        WHEN c.`user_agent` LIKE '%Linux%' THEN 'Linux'
        WHEN c.`user_agent` LIKE '%Android%' THEN 'Android'
        WHEN c.`user_agent` LIKE '%iPhone%' OR c.`user_agent` LIKE '%iPad%' OR c.`user_agent` LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE c.`user_agent` IS NOT NULL AND c.`user_agent` != '';

-- =====================================================
-- 步骤6: 数据修复脚本 - 回填 bbs_comment_replies
-- =====================================================
UPDATE `bbs_comment_replies` r
SET
    r.`terminal_type` = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN r.`user_agent` LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN r.`user_agent` LIKE '%Android%' OR r.`user_agent` LIKE '%iPhone%' OR
             r.`user_agent` LIKE '%iPad%' OR r.`user_agent` LIKE '%Mobile%' OR
             r.`user_agent` LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    r.`platform` = CASE
        WHEN r.`user_agent` LIKE '%Windows%' THEN 'Windows'
        WHEN r.`user_agent` LIKE '%Mac%' THEN 'Mac'
        WHEN r.`user_agent` LIKE '%Linux%' THEN 'Linux'
        WHEN r.`user_agent` LIKE '%Android%' THEN 'Android'
        WHEN r.`user_agent` LIKE '%iPhone%' OR r.`user_agent` LIKE '%iPad%' OR r.`user_agent` LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE r.`user_agent` IS NOT NULL AND r.`user_agent` != '';

-- =====================================================
-- 步骤7: 更新 bbs_daily_stats 历史数据中的 platform 值
-- =====================================================
-- 将 iOS 更新为 iPhone（统一为设备类型）
UPDATE `bbs_daily_stats`
SET `platform` = 'iPhone'
WHERE `platform` = 'iOS';
