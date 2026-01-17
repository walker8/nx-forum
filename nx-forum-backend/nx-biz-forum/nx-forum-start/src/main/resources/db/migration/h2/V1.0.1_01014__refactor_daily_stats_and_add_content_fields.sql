-- =====================================================
-- 重构每日数据统计表：为内容表添加终端类型和平台字段
-- =====================================================

-- =====================================================
-- 步骤1: 为 bbs_threads 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE bbs_threads
ADD COLUMN terminal_type varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）';
ALTER TABLE bbs_threads
ADD COLUMN platform varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）';

-- 添加索引以提高查询性能
CREATE INDEX idx_create_time_terminal ON bbs_threads (create_time, terminal_type);
CREATE INDEX idx_create_time_platform ON bbs_threads (create_time, platform);
CREATE INDEX idx_create_time_terminal_platform ON bbs_threads (create_time, terminal_type, platform);

-- =====================================================
-- 步骤2: 为 bbs_comments 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE bbs_comments
ADD COLUMN terminal_type varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）';
ALTER TABLE bbs_comments
ADD COLUMN platform varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）';

-- 添加索引
CREATE INDEX idx_comments_create_time_terminal ON bbs_comments (create_time, terminal_type);
CREATE INDEX idx_comments_create_time_platform ON bbs_comments (create_time, platform);
CREATE INDEX idx_comments_create_time_terminal_platform ON bbs_comments (create_time, terminal_type, platform);

-- =====================================================
-- 步骤3: 为 bbs_comment_replies 表添加 terminal_type 和 platform 字段
-- =====================================================
ALTER TABLE bbs_comment_replies
ADD COLUMN terminal_type varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）';
ALTER TABLE bbs_comment_replies
ADD COLUMN platform varchar(50) NOT NULL DEFAULT '' COMMENT '平台（Windows/Mac/Android/iPhone等）';

-- 添加索引
CREATE INDEX idx_replies_create_time_terminal ON bbs_comment_replies (create_time, terminal_type);
CREATE INDEX idx_replies_create_time_platform ON bbs_comment_replies (create_time, platform);
CREATE INDEX idx_replies_create_time_terminal_platform ON bbs_comment_replies (create_time, terminal_type, platform);

-- =====================================================
-- 步骤4: 数据修复脚本 - 回填 bbs_threads
-- =====================================================
UPDATE bbs_threads
SET
    terminal_type = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN user_agent LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN user_agent LIKE '%Android%' OR user_agent LIKE '%iPhone%' OR
             user_agent LIKE '%iPad%' OR user_agent LIKE '%Mobile%' OR
             user_agent LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    platform = CASE
        WHEN user_agent LIKE '%Windows%' THEN 'Windows'
        WHEN user_agent LIKE '%Mac%' THEN 'Mac'
        WHEN user_agent LIKE '%Linux%' THEN 'Linux'
        WHEN user_agent LIKE '%Android%' THEN 'Android'
        WHEN user_agent LIKE '%iPhone%' OR user_agent LIKE '%iPad%' OR user_agent LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE user_agent IS NOT NULL AND user_agent != '';

-- =====================================================
-- 步骤5: 数据修复脚本 - 回填 bbs_comments
-- =====================================================
UPDATE bbs_comments
SET
    terminal_type = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN user_agent LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN user_agent LIKE '%Android%' OR user_agent LIKE '%iPhone%' OR
             user_agent LIKE '%iPad%' OR user_agent LIKE '%Mobile%' OR
             user_agent LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    platform = CASE
        WHEN user_agent LIKE '%Windows%' THEN 'Windows'
        WHEN user_agent LIKE '%Mac%' THEN 'Mac'
        WHEN user_agent LIKE '%Linux%' THEN 'Linux'
        WHEN user_agent LIKE '%Android%' THEN 'Android'
        WHEN user_agent LIKE '%iPhone%' OR user_agent LIKE '%iPad%' OR user_agent LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE user_agent IS NOT NULL AND user_agent != '';

-- =====================================================
-- 步骤6: 数据修复脚本 - 回填 bbs_comment_replies
-- =====================================================
UPDATE bbs_comment_replies
SET
    terminal_type = CASE
        -- 1. 新标准：NXForumApp/ 前缀检测
        WHEN user_agent LIKE '%NXForumApp/%'
        THEN 'APP'
        -- 2. 移动端检测
        WHEN user_agent LIKE '%Android%' OR user_agent LIKE '%iPhone%' OR
             user_agent LIKE '%iPad%' OR user_agent LIKE '%Mobile%' OR
             user_agent LIKE '%Touch%'
        THEN 'MOBILE'
        -- 3. 默认PC
        ELSE 'PC'
    END,
    platform = CASE
        WHEN user_agent LIKE '%Windows%' THEN 'Windows'
        WHEN user_agent LIKE '%Mac%' THEN 'Mac'
        WHEN user_agent LIKE '%Linux%' THEN 'Linux'
        WHEN user_agent LIKE '%Android%' THEN 'Android'
        WHEN user_agent LIKE '%iPhone%' OR user_agent LIKE '%iPad%' OR user_agent LIKE '%iOS%' THEN 'iPhone'
        ELSE ''
    END
WHERE user_agent IS NOT NULL AND user_agent != '';

-- =====================================================
-- 步骤7: 更新 bbs_daily_stats 历史数据中的 platform 值
-- =====================================================
-- 将 iOS 更新为 iPhone（统一为设备类型）
UPDATE bbs_daily_stats
SET platform = 'iPhone'
WHERE platform = 'iOS';
