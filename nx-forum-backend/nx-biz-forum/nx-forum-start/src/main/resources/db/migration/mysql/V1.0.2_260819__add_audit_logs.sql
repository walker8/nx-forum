-- =====================================================
-- 内容审核记录表
-- 用于记录主贴/评论/楼中楼的完整审核轨迹
-- 包括：黑白名单、敏感词、规则引擎、AI、人工操作的命中详情
-- =====================================================

-- ----------------------------
-- Table structure for bbs_audit_logs
-- ----------------------------
CREATE TABLE `bbs_audit_logs` (
  `log_id`              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `content_type`        tinyint unsigned NOT NULL COMMENT '内容类型 1=主贴 2=评论 3=楼中楼',
  `content_id`          bigint unsigned DEFAULT NULL COMMENT '内容ID(同步阶段未保存前为NULL)',
  `forum_id`            int unsigned NOT NULL COMMENT '版块ID',
  `user_id`             bigint unsigned NOT NULL COMMENT '内容作者ID',
  `user_ip`             varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者IP',
  `audit_session_id`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核会话ID(同步+AI复审共享)',
  `trigger_source`      tinyint unsigned NOT NULL DEFAULT 1 COMMENT '触发来源 1=主动 2=编辑 3=举报 4=管理员后台',
  `audit_stage`         tinyint unsigned NOT NULL COMMENT '审核阶段 1=黑白名单 2=敏感词 3=规则引擎 4=AI 5=人工',
  `audit_status_before` tinyint unsigned DEFAULT NULL COMMENT '审核前状态 0通过 1审核中 2拒绝 3忽略',
  `audit_status_after`  tinyint unsigned NOT NULL COMMENT '审核后状态',
  `hit_detail`          json DEFAULT NULL COMMENT '命中详情JSON(按stage区分)',
  `operator_id`         bigint unsigned DEFAULT NULL COMMENT '操作人ID 系统=NULL 管理员=用户ID',
  `operator_type`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '1=系统 2=管理员',
  `reason`              varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核原因摘要',
  `cost_ms`             int unsigned DEFAULT NULL COMMENT '审核耗时(毫秒)',
  `content_snapshot`    text COMMENT '内容快照(便于内容被改/删后回溯)',
  `create_by`           bigint unsigned DEFAULT NULL COMMENT '创建者',
  `create_time`         datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_by`           bigint unsigned DEFAULT NULL COMMENT '更新者',
  `update_time`         datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_deleted`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除 0存在 1删除',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `idx_content` (`content_type`, `content_id`, `create_time`) USING BTREE,
  KEY `idx_user_time` (`user_id`, `create_time`) USING BTREE,
  KEY `idx_forum_status_time` (`forum_id`, `audit_status_after`, `create_time`) USING BTREE,
  KEY `idx_stage_time` (`audit_stage`, `create_time`) USING BTREE,
  KEY `idx_session` (`audit_session_id`) USING BTREE,
  KEY `idx_operator` (`operator_id`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='内容审核记录表';
