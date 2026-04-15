-- uc_role_permissions: 将 perm_id 关联改为 perms 字符串关联
-- 新增 perms 列
ALTER TABLE uc_role_permissions ADD perms varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识' BEFORE create_by;

-- 从 uc_permissions 回填 perms 值
UPDATE uc_role_permissions rp
    SET rp.perms = (SELECT p.perms FROM uc_permissions p WHERE p.perm_id = rp.perm_id AND p.is_deleted = 0)
WHERE rp.is_deleted = 0;

-- 删除 perm_id 列
ALTER TABLE uc_role_permissions DROP COLUMN perm_id;

-- 添加索引优化按角色查询权限的性能
CREATE INDEX idx_role_permissions_role_key ON uc_role_permissions (role_key, is_deleted);
