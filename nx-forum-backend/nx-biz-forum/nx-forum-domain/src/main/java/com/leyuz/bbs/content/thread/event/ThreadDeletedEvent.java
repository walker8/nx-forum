package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.ThreadE;

import java.io.Serial;

public class ThreadDeletedEvent extends BaseEvent<ThreadE> {

    @Serial
    private static final long serialVersionUID = 120165834479746183L;

    public ThreadDeletedEvent(Object source, ThreadE eventData, String reason, boolean notice) {
        super(source, eventData, reason, notice);
    }
}
