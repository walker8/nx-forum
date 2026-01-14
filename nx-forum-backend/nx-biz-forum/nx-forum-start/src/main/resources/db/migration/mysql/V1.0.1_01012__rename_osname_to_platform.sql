-- Rename os_name to platform in bbs_access_logs
ALTER TABLE `bbs_access_logs` CHANGE COLUMN `os_name` `platform` varchar(50) NOT NULL DEFAULT '' COMMENT '平台名称（Windows/Android/iOS等）';
ALTER TABLE `bbs_access_logs` DROP INDEX `idx_os_name`;
ALTER TABLE `bbs_access_logs` ADD INDEX `idx_platform` (`platform`) USING BTREE;

-- Rename os_name to platform in bbs_daily_stats
ALTER TABLE `bbs_daily_stats` CHANGE COLUMN `os_name` `platform` varchar(50) NOT NULL DEFAULT 'ALL' COMMENT '平台（ALL表示全部）';
ALTER TABLE `bbs_daily_stats` DROP INDEX `uk_date_terminal_os`;
ALTER TABLE `bbs_daily_stats` ADD UNIQUE KEY `uk_date_terminal_platform` (`stat_date`, `terminal_type`, `platform`) USING BTREE;
