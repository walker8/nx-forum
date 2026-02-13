-- Add layout field to bbs_custom_pages table
ALTER TABLE bbs_custom_pages ADD COLUMN layout VARCHAR(20) NOT NULL DEFAULT 'default';
