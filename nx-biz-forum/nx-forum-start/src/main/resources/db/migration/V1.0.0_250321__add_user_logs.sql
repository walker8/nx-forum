-- 用户操作日志表
CREATE TABLE `uc_user_logs` (
    `log_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `log_type` tinyint unsigned NOT NULL COMMENT '日志类型（1登录 2登出 3注册 4信息修改 5密码修改）',
    `log_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '日志内容',
    `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '操作IP地址',
    `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户代理信息',
    `operation_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '操作状态（0成功 1失败）',
    `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `uc_user_logs_create_by_IDX` (`create_by`) USING BTREE,
    KEY `uc_user_logs_create_time_IDX` (`create_time`) USING BTREE,
    KEY `uc_user_logs_log_type_IDX` (`log_type`) USING BTREE,
    KEY `uc_user_logs_ip_address_IDX` (`ip_address`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户操作日志表';