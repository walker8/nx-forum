package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.ThreadPropertyE;

import java.io.Serial;

public class ThreadChangePropertyEvent extends BaseEvent<ThreadPropertyE> {

    @Serial
    private static final long serialVersionUID = 7043031912657421182L;

    public ThreadChangePropertyEvent(Object source, ThreadPropertyE eventData) {
        super(source, eventData);
    }
}
