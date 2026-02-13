-- Add layout field to bbs_custom_pages table
ALTER TABLE bbs_custom_pages ADD COLUMN layout VARCHAR(20) NOT NULL DEFAULT 'default' COMMENT '布局类型（default默认 simple简洁 empty无布局）';
