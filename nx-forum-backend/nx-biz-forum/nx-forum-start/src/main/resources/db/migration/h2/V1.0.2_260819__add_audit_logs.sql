-- =====================================================
-- 内容审核记录表 (H2 版本)
-- 用于记录主贴/评论/楼中楼的完整审核轨迹
-- =====================================================

CREATE TABLE bbs_audit_logs (
  log_id              bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  content_type        tinyint NOT NULL COMMENT '内容类型 1=主贴 2=评论 3=楼中楼',
  content_id          bigint DEFAULT NULL COMMENT '内容ID(同步阶段未保存前为NULL)',
  forum_id            int NOT NULL COMMENT '版块ID',
  user_id             bigint NOT NULL COMMENT '内容作者ID',
  user_ip             varchar(40) DEFAULT NULL COMMENT '作者IP',
  audit_session_id    varchar(32) DEFAULT NULL COMMENT '审核会话ID(同步+AI复审共享)',
  trigger_source      tinyint NOT NULL DEFAULT 1 COMMENT '触发来源 1=主动 2=编辑 3=举报 4=管理员后台',
  audit_stage         tinyint NOT NULL COMMENT '审核阶段 1=黑白名单 2=敏感词 3=规则引擎 4=AI 5=人工',
  audit_status_before tinyint DEFAULT NULL COMMENT '审核前状态 0通过 1审核中 2拒绝 3忽略',
  audit_status_after  tinyint NOT NULL COMMENT '审核后状态',
  hit_detail          varchar(4000) DEFAULT NULL COMMENT '命中详情JSON(按stage区分,文本存储)',
  operator_id         bigint DEFAULT NULL COMMENT '操作人ID 系统=NULL 管理员=用户ID',
  operator_type       tinyint NOT NULL DEFAULT 1 COMMENT '1=系统 2=管理员',
  reason              varchar(500) DEFAULT NULL COMMENT '审核原因摘要',
  cost_ms             int DEFAULT NULL COMMENT '审核耗时(毫秒)',
  content_snapshot    clob COMMENT '内容快照(便于内容被改/删后回溯)',
  create_by           bigint DEFAULT NULL COMMENT '创建者',
  create_time         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by           bigint DEFAULT NULL COMMENT '更新者',
  update_time         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted          tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0存在 1删除',
  PRIMARY KEY (log_id),
  KEY bbs_audit_logs_content_idx (content_type, content_id, create_time),
  KEY bbs_audit_logs_user_time_idx (user_id, create_time),
  KEY bbs_audit_logs_forum_status_time_idx (forum_id, audit_status_after, create_time),
  KEY bbs_audit_logs_stage_time_idx (audit_stage, create_time),
  KEY bbs_audit_logs_session_idx (audit_session_id),
  KEY bbs_audit_logs_operator_idx (operator_id, create_time)

);
