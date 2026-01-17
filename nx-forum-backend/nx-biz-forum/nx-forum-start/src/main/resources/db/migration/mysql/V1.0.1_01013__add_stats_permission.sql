-- 添加统计数据权限
INSERT INTO `uc_permissions` VALUES (
    49,                                    -- perm_id
    12,                                    -- parent_id (管理权限/admin:manage)
    '统计数据',                             -- perm_name
    0,                                     -- perm_status (0-正常)
    'admin:stats',                         -- perms
    2,                                     -- perm_type (2-菜单)
    4,                                     -- perm_order
    '论坛统计数据查询权限',                  -- remark
    1,                                     -- create_by
    NOW(),                                 -- create_time
    1,                                     -- update_by
    NOW(),                                 -- update_time
    0                                      -- is_deleted
);

-- 为ADMIN角色分配权限
INSERT INTO `uc_role_permissions` VALUES (
    NULL,                                  -- id (auto-increment)
    'ADMIN',
    49,                                    -- perm_id for admin:stats
    1,
    NOW(),
    1,
    NOW(),
    0
);
