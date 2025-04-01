package com.leyuz.uc.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户日志持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("uc_user_logs")
public class UserLogPO {
    
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    
    /**
     * 日志类型（1登录 2注册 3信息修改 4密码修改）
     */
    @TableField("log_type")
    private Integer logType;
    
    /**
     * 日志内容
     */
    @TableField("log_content")
    private String logContent;
    
    /**
     * 操作IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;
    
    /**
     * 操作状态（0成功 1失败）
     */
    @TableField("operation_status")
    private Integer operationStatus;
    
    /**
     * 创建者（用户ID）
     */
    @TableField("create_by")
    private Long createBy;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
} 