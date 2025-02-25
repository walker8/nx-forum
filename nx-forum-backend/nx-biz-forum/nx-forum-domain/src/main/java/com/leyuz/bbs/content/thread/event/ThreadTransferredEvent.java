package com.leyuz.bbs.content.thread.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.thread.event.dto.ThreadTransferredEventData;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ThreadTransferredEvent extends BaseEvent<ThreadTransferredEventData> {

    @Serial
    private static final long serialVersionUID = -7940338540771719833L;

    public ThreadTransferredEvent(Object source, ThreadTransferredEventData eventData, boolean notice) {
        super(source, eventData, "", notice);
    }
} 