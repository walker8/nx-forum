package com.leyuz.bbs.system.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditDTO {
    private Long userId;
    private String userName;
    private String message;
}
