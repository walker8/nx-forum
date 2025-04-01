package com.leyuz.uc.domain.log;

import com.leyuz.uc.domain.log.dataobject.LogTypeV;
import com.leyuz.uc.domain.log.dataobject.OperationStatusV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogE {
    
    /**
     * 日志ID
     */
    private Long logId;
    
    /**
     * 日志类型（1登录 2注册 3信息修改 4密码修改）
     */
    private LogTypeV logType;
    
    /**
     * 日志内容
     */
    private String logContent;
    
    /**
     * 操作IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理信息
     */
    private String userAgent;
    
    /**
     * 操作状态（0成功 1失败）
     */
    private OperationStatusV operationStatus;
    
    /**
     * 创建者（用户ID）
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 记录日志
     */
    public void record() {
        createTime = LocalDateTime.now();
    }
} 