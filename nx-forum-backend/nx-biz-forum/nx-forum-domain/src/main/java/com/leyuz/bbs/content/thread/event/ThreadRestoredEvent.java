package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.ThreadE;

import java.io.Serial;

public class ThreadRestoredEvent extends BaseEvent<ThreadE> {

    @Serial
    private static final long serialVersionUID = 5368179092080660809L;

    public ThreadRestoredEvent(Object source, ThreadE eventData, boolean notice) {
        super(source, eventData, "", notice);
    }
}
