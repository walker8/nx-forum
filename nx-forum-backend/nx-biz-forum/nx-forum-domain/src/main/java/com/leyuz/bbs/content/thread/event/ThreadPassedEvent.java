package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.ThreadE;

import java.io.Serial;

public class ThreadPassedEvent extends BaseEvent<ThreadE> {

    @Serial
    private static final long serialVersionUID = 6060527256726337303L;

    public ThreadPassedEvent(Object source, ThreadE eventData, Boolean notice) {
        super(source, eventData, "", notice);
    }
}
