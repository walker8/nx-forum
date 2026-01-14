-- 访问日志表
CREATE TABLE `bbs_access_logs` (
  `log_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `access_time` datetime NOT NULL COMMENT '访问时间',
  `ip_address` varchar(40) NOT NULL DEFAULT '' COMMENT 'IP地址',
  `device_id` varchar(64) NOT NULL DEFAULT '' COMMENT '设备ID（前端传值，Web端为空）',
  `app_version` varchar(20) NOT NULL DEFAULT '' COMMENT 'App版本号（Web端为空）',
  `user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '用户ID（0表示未登录）',
  `user_agent` varchar(500) NOT NULL DEFAULT '' COMMENT 'User-Agent原始值',
  `terminal_type` varchar(20) NOT NULL DEFAULT '' COMMENT '终端类型（PC/MOBILE/APP）',
  `os_name` varchar(50) NOT NULL DEFAULT '' COMMENT '操作系统名称（Windows/Android/iOS等）',
  `browser_name` varchar(50) NOT NULL DEFAULT '' COMMENT '浏览器名称（Chrome/Safari等）',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `idx_access_time` (`access_time`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_terminal_type` (`terminal_type`) USING BTREE,
  KEY `idx_os_name` (`os_name`) USING BTREE,
  KEY `idx_access_time_ip` (`access_time`, `ip_address`) USING BTREE,
  KEY `idx_access_time_terminal` (`access_time`, `terminal_type`) USING BTREE,
  KEY `idx_access_time_user` (`access_time`, `user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='访问日志表';

-- 每日统计汇总表
CREATE TABLE `bbs_daily_stats` (
  `stat_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `unique_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '独立IP访问数',
  `guest_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '游客独立IP数',
  `user_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '登录用户独立IP数',
  `thread_count` int unsigned NOT NULL DEFAULT 0 COMMENT '发帖数量',
  `comment_count` int unsigned NOT NULL DEFAULT 0 COMMENT '回帖数量',
  `terminal_type` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '终端类型（ALL/PC/MOBILE/APP）',
  `os_name` varchar(50) NOT NULL DEFAULT 'ALL' COMMENT '操作系统（ALL表示全部）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stat_id`) USING BTREE,
  UNIQUE KEY `uk_date_terminal_os` (`stat_date`, `terminal_type`, `os_name`) USING BTREE,
  KEY `idx_stat_date` (`stat_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='每日统计汇总表';

-- 每小时访问统计表
CREATE TABLE `bbs_hourly_stats` (
  `stat_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_hour` tinyint unsigned NOT NULL COMMENT '统计小时（0-23）',
  `unique_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '独立IP数',
  `guest_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '游客独立IP数',
  `user_ip_count` int unsigned NOT NULL DEFAULT 0 COMMENT '登录用户独立IP数',
  `terminal_type` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '终端类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stat_id`) USING BTREE,
  UNIQUE KEY `uk_date_hour_terminal` (`stat_date`, `stat_hour`, `terminal_type`) USING BTREE,
  KEY `idx_stat_date_hour` (`stat_date`, `stat_hour`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='每小时访问统计表';
