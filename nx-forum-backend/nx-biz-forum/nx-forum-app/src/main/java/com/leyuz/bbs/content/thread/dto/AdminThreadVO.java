package com.leyuz.bbs.content.thread.dto;

import lombok.Data;

import java.io.Serial;

@Data
public class AdminThreadVO extends ThreadVO {
    @Serial
    private static final long serialVersionUID = 3837297780341530448L;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private Byte auditStatus;

    /**
     * 审核原因
     */
    private String auditReason;
    private boolean recommended;

    private String userIp;
    private String location;
    private String browser;
    private String os;

    @Override
    public void setRecommendProperty(boolean recommended) {
        setRecommended(recommended);
    }
}
