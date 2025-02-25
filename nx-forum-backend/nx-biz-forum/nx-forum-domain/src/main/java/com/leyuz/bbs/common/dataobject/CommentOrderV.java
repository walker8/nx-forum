package com.leyuz.bbs.common.dataobject;

public enum CommentOrderV {
    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    TIME_ASC(0), TIME_DESC(1), HOT(2);

    private final int value;

    CommentOrderV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CommentOrderV of(int value) {
        for (CommentOrderV commentOrderV : CommentOrderV.values()) {
            if (commentOrderV.getValue() == value) {
                return commentOrderV;
            }
        }
        return null;
    }
}
