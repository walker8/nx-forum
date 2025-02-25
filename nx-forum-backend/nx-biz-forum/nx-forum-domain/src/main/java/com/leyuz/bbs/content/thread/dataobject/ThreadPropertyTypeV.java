package com.leyuz.bbs.content.thread.dataobject;

public enum ThreadPropertyTypeV {

    TOP(1), DIGEST(2), CLOSED(3), RECOMMEND(4);

    private final int value;

    ThreadPropertyTypeV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ThreadPropertyTypeV of(int value) {
        for (ThreadPropertyTypeV threadPropertyTypeV : ThreadPropertyTypeV.values()) {
            if (threadPropertyTypeV.getValue() == value) {
                return threadPropertyTypeV;
            }
        }
        return null;
    }
}
