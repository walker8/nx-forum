package com.leyuz.bbs.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

@Getter
public class BaseEvent<T> extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -808232341604216261L;
    private final T eventData;
    // 是否通知用户
    private final boolean notice;
    // 原因
    private final String reason;

    public BaseEvent(Object source, T eventData, String reason, boolean notice) {
        super(source);
        this.eventData = eventData;
        this.notice = notice;
        this.reason = reason;
    }

    public BaseEvent(Object source, T eventData) {
        super(source);
        this.eventData = eventData;
        notice = true;
        reason = "";
    }
}

