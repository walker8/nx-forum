package com.leyuz.bbs.common.dataobject;

/**
 * 审核阶段
 * <p>
 * 1 黑白名单 2 敏感词 3 规则引擎 4 AI 5 人工
 * </p>
 */
public enum AuditStageV {
    /**
     * 1 黑白名单
     */
    BLACK_WHITE(1),
    /**
     * 2 敏感词
     */
    SENSITIVE_WORD(2),
    /**
     * 3 规则引擎
     */
    RULE_ENGINE(3),
    /**
     * 4 AI
     */
    AI(4),
    /**
     * 5 人工
     */
    MANUAL(5);

    /**
     * 根据字节值转换为枚举
     *
     * @param value 数据库存储值
     * @return 对应枚举，未匹配返回 null
     */
    public static AuditStageV of(Byte value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 1 -> BLACK_WHITE;
            case 2 -> SENSITIVE_WORD;
            case 3 -> RULE_ENGINE;
            case 4 -> AI;
            case 5 -> MANUAL;
            default -> null;
        };
    }

    private final int value;

    AuditStageV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
