package com.leyuz.uc.log.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.uc.log.UserLogPO;

/**
 * 用户日志服务接口
 */
public interface IUserLogService extends IService<UserLogPO> {
    
    /**
     * 分页查询用户日志
     *
     * @param pageQuery 分页查询参数
     * @return 用户日志分页结果
     */
    Page<UserLogPO> queryUserLogs(PageQuery pageQuery);
} 