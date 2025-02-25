package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.ThreadE;

import java.io.Serial;

public class ThreadRejectedEvent extends BaseEvent<ThreadE> {

    @Serial
    private static final long serialVersionUID = 5284164437268983009L;

    public ThreadRejectedEvent(Object source, ThreadE eventData, String reason, boolean notice) {
        super(source, eventData, reason, notice);
    }
}
