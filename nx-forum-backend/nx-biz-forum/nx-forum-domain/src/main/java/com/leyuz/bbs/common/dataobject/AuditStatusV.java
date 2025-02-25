package com.leyuz.bbs.common.dataobject;

public enum AuditStatusV {
    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    PASSED(0), AUDITING(1), REJECTED(2);

    public static AuditStatusV of(Byte value) {
        if (value == null) {
            return PASSED;
        }
        return switch (value) {
            case 1 -> AUDITING;
            case 2 -> REJECTED;
            default -> PASSED;
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
