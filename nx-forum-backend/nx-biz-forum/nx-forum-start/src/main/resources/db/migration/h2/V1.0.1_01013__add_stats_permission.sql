-- 添加统计数据权限
INSERT INTO uc_permissions VALUES (
    49,
    12,
    '统计数据',
    0,
    'admin:stats',
    2,
    4,
    '论坛统计数据查询权限',
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP,
    0
);

-- 为ADMIN角色分配权限
INSERT INTO uc_role_permissions VALUES (
    NULL,
    'ADMIN',
    49,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP,
    0
);
