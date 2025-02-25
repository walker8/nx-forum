package com.leyuz.bbs.thread.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.thread.ThreadPropertyPO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
public interface IThreadPropertyService extends IService<ThreadPropertyPO> {
    boolean deleteByThreadId(Long threadId, int propertyType);
}
