-- Rename os_name to platform in bbs_access_logs
ALTER TABLE `bbs_access_logs` ALTER COLUMN `os_name` RENAME TO `platform`;
COMMENT ON COLUMN `bbs_access_logs`.`platform` IS '平台名称（Windows/Android/iOS等）';

-- Rename os_name to platform in bbs_daily_stats
ALTER TABLE `bbs_daily_stats` ALTER COLUMN `os_name` RENAME TO `platform`;
COMMENT ON COLUMN `bbs_daily_stats`.`platform` IS '平台（ALL表示全部）';
