package com.leyuz.bbs.domain.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.domain.thread.ThreadE;

import java.io.Serial;

public class ThreadNewEvent extends BaseEvent<ThreadE> {

    @Serial
    private static final long serialVersionUID = 120165834479746183L;

    public ThreadNewEvent(Object source, ThreadE eventData) {
        super(source, eventData);
    }
}
