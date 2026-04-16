package com.leyuz.bbs.interaction.report.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.interaction.report.model.ReportE;

import java.io.Serial;

/**
 * 举报处理事件
 */
public class ReportHandledEvent extends BaseEvent<ReportE> {

    @Serial
    private static final long serialVersionUID = 1L;

    public ReportHandledEvent(Object source, ReportE eventData) {
        super(source, eventData);
    }
}
