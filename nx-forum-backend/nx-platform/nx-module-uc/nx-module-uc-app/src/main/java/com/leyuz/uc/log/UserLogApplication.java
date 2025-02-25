package com.leyuz.uc.log;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.ip.AddressUtils;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.UserAgentUtils;
import com.leyuz.uc.log.dto.LogTypeV;
import com.leyuz.uc.log.dto.OperationStatusV;
import com.leyuz.uc.log.dto.UserLogQuery;
import com.leyuz.uc.log.dto.UserLogVO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户日志应用服务（MVC 模式）
 * 负责用户操作日志的记录和查询
 *
 * @author walker
 * @since 2024-01-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLogApplication {

    private final UserLogMapper userLogMapper;

    /**
     * 记录用户日志（使用当前登录用户）
     *
     * @param logType         日志类型
     * @param logContent      日志内容
     * @param operationStatus 操作状态
     */
    public void recordLog(LogTypeV logType, String logContent, OperationStatusV operationStatus) {
        Long userId = HeaderUtils.getUserId();
        String ipAddress = HeaderUtils.getIp();
        String userAgent = HeaderUtils.getUserAgent();

        recordLog(userId, logType, logContent, ipAddress, userAgent, operationStatus);
    }

    /**
     * 记录用户日志（指定用户ID）
     *
     * @param userId          用户ID
     * @param logType         日志类型
     * @param logContent      日志内容
     * @param ipAddress       IP地址
     * @param userAgent       用户代理
     * @param operationStatus 操作状态
     */
    public void recordLog(Long userId, LogTypeV logType, String logContent, String ipAddress, String userAgent, OperationStatusV operationStatus) {
        UserLogPO userLogPO = UserLogPO.builder()
                .logType(logType.getCode())
                .logContent(logContent)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .operationStatus(operationStatus.getCode())
                .createBy(userId)
                .createTime(java.time.LocalDateTime.now())
                .build();

        userLogMapper.insert(userLogPO);
    }

    /**
     * 记录登录日志
     *
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordLoginLog(boolean success, String message) {
        recordLog(LogTypeV.LOGIN, message, success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 记录登录日志（指定用户ID）
     *
     * @param userId  用户ID
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordLoginLog(Long userId, boolean success, String message) {
        String ipAddress = HeaderUtils.getIp();
        String userAgent = HeaderUtils.getUserAgent();
        recordLog(userId, LogTypeV.LOGIN, message, ipAddress, userAgent,
                success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 记录注册日志
     *
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordRegisterLog(boolean success, String message) {
        recordLog(LogTypeV.REGISTER, message, success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 记录注册日志（指定用户ID）
     *
     * @param userId  用户ID
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordRegisterLog(Long userId, boolean success, String message) {
        String ipAddress = HeaderUtils.getIp();
        String userAgent = HeaderUtils.getUserAgent();
        recordLog(userId, LogTypeV.REGISTER, message, ipAddress, userAgent,
                success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 记录信息修改日志
     *
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordInfoUpdateLog(boolean success, String message) {
        recordLog(LogTypeV.INFO_UPDATE, message, success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 记录密码修改日志
     *
     * @param success 是否成功
     * @param message 消息内容
     */
    public void recordPasswordUpdateLog(boolean success, String message) {
        recordLog(LogTypeV.PASSWORD_UPDATE, message, success ? OperationStatusV.SUCCESS : OperationStatusV.FAILURE);
    }

    /**
     * 查询用户日志
     *
     * @param query 查询条件
     * @return 用户日志分页结果
     */
    public CustomPage<UserLogVO> queryUserLogs(UserLogQuery query) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(query.getPageNo())
                .pageSize(query.getPageSize())
                .build();

        Map<String, Object> params = pageQuery.getParams();
        params.put("userId", query.getUserId());
        params.put("ipAddress", query.getIpAddress());

        // 如果logType不为空，转换为Integer
        if (query.getLogType() != null) {
            params.put("logType", query.getLogType());
        }

        Page<UserLogPO> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        Page<UserLogPO> userLogPOPage = userLogMapper.queryUserLogs(page, params);

        return DataBaseUtils.createCustomPage(userLogPOPage, this::toUserLogVO);
    }

    /**
     * 将持久化对象转换为展示对象
     */
    private UserLogVO toUserLogVO(UserLogPO userLogPO) {
        UserLogVO userLogVO = new UserLogVO();
        userLogVO.setLogId(userLogPO.getLogId());
        userLogVO.setLogType(userLogPO.getLogType());
        userLogVO.setLogContent(userLogPO.getLogContent());
        userLogVO.setIpAddress(userLogPO.getIpAddress());
        userLogVO.setLocation(AddressUtils.getCityByIP(userLogPO.getIpAddress()));
        UserClientInfo clientInfo = UserAgentUtils.getClientInfo(userLogPO.getUserAgent());
        userLogVO.setOs(clientInfo.getOs());
        userLogVO.setBrowser(clientInfo.getBrowser());
        userLogVO.setOperationStatus(userLogPO.getOperationStatus());
        userLogVO.setUserId(userLogPO.getCreateBy());
        userLogVO.setCreateTime(userLogPO.getCreateTime());

        // 补充日志类型描述
        LogTypeV logTypeV = LogTypeV.fromCode(userLogPO.getLogType());
        if (logTypeV != null) {
            userLogVO.setLogTypeDesc(logTypeV.getDesc());
        }

        // 补充操作状态描述
        OperationStatusV operationStatusV = OperationStatusV.fromCode(userLogPO.getOperationStatus());
        if (operationStatusV != null) {
            userLogVO.setOperationStatusDesc(operationStatusV.getDesc());
        }

        // 从缓存中获取用户名
        if (userLogPO.getCreateBy() != null && userLogPO.getCreateBy() > 0) {
            userLogVO.setUserId(userLogPO.getCreateBy());
            UserE userE = SpringUtil.getBean(UserApplication.class).getByIdFromCache(userLogPO.getCreateBy());
            userLogVO.setUserName(userE.getUserName());
        }

        return userLogVO;
    }
} 