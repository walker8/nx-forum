package com.leyuz.bbs.common.dataobject;

public enum AuditStatusV {
    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    PASSED(0), AUDITING(1), REJECTED(2);

    /**
     * 根据字节值转换为枚举；null 与未识别值返回 null，避免「审核前状态：通过」的误导显示
     * （与 AuditStageV.of(null) 行为对齐）。
     */
    public static AuditStatusV of(Byte value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 0 -> PASSED;
            case 1 -> AUDITING;
            case 2 -> REJECTED;
            default -> null;
        };
    }

    private final int value;

    AuditStatusV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
