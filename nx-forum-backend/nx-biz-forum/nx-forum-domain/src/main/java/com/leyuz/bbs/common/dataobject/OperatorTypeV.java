package com.leyuz.bbs.common.dataobject;

/**
 * 审核操作人类型
 * <p>
 * 1 系统 2 管理员
 * </p>
 */
public enum OperatorTypeV {
    /**
     * 1 系统自动
     */
    SYSTEM(1),
    /**
     * 2 管理员人工
     */
    ADMIN(2);

    /**
     * 根据字节值转换为枚举
     *
     * @param value 数据库存储值
     * @return 对应枚举，未匹配返回 null
     */
    public static OperatorTypeV of(Byte value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 1 -> SYSTEM;
            case 2 -> ADMIN;
            default -> null;
        };
    }

    private final int value;

    OperatorTypeV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
