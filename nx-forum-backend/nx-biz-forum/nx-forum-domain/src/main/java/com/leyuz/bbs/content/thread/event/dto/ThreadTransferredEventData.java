package com.leyuz.bbs.content.thread.event.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadTransferredEventData {
    private Long threadId;
    private Integer targetForumId;
    private Long userId;
    private String subject;
    private String brief;
}
