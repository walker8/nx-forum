package com.leyuz.bbs.content.thread.dataobject;

import lombok.Data;

@Data
public class ThreadPropertyV {
    /**
     * 是否置顶
     */
    private int top;
    /**
     * 是否精华
     */
    private boolean digest;
    /**
     * 是否屏蔽
     */
    private boolean blocked;
    /**
     * 是否推荐
     */
    private boolean recommended;

    /**
     * 是否关闭
     */
    private boolean closed;

    public void init() {
        top = 0;
        digest = false;
        blocked = false;
        recommended = false;
        closed = false;
    }

    public boolean getValue(ThreadPropertyTypeV propertyType) {
        if (propertyType == ThreadPropertyTypeV.TOP) {
            return top > 0;
        } else if (propertyType == ThreadPropertyTypeV.DIGEST) {
            return digest;
        } else if (propertyType == ThreadPropertyTypeV.RECOMMEND) {
            return recommended;
        }
        return false;
    }

    /**
     * 是否需要入库
     *
     * @param propertyType
     * @return
     */
    public boolean isProperty(ThreadPropertyTypeV propertyType) {
        if (propertyType == ThreadPropertyTypeV.TOP ||
                propertyType == ThreadPropertyTypeV.DIGEST ||
                propertyType == ThreadPropertyTypeV.RECOMMEND) {
            return true;
        }
        return false;
    }

    public boolean isTop() {
        return top > 0;
    }
}
