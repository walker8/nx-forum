package com.leyuz.bbs.content.thread.dataobject;

public enum ThreadPropertyAttributeTopV implements ThreadPropertyAttribute {

    GLOBAL(0), CURRENT_FORUM(1);

    private final int value;

    ThreadPropertyAttributeTopV(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public static ThreadPropertyAttributeTopV of(int value) {
        for (ThreadPropertyAttributeTopV threadPropertyTypeV : ThreadPropertyAttributeTopV.values()) {
            if (threadPropertyTypeV.getValue() == value) {
                return threadPropertyTypeV;
            }
        }
        return null;
    }
}
