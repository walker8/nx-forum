package com.leyuz.bbs.common.dataobject;

/**
 * 审核触发来源
 * <p>
 * 1 主动 2 编辑 3 举报 4 管理员后台
 * </p>
 */
public enum AuditTriggerSourceV {
    /**
     * 1 主动（用户发帖/评论/回复）
     */
    NEW(1),
    /**
     * 2 编辑（用户编辑触发复审）
     */
    EDIT(2),
    /**
     * 3 举报（用户举报触发审核）
     */
    REPORT(3),
    /**
     * 4 管理员后台操作
     */
    MANUAL(4);

    /**
     * 根据字节值转换为枚举
     *
     * @param value 数据库存储值
     * @return 对应枚举，未匹配返回 null
     */
    public static AuditTriggerSourceV of(Byte value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 1 -> NEW;
            case 2 -> EDIT;
            case 3 -> REPORT;
            case 4 -> MANUAL;
            default -> null;
        };
    }

    private final int value;

    AuditTriggerSourceV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
