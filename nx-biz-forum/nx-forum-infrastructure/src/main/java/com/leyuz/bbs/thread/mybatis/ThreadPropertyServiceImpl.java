package com.leyuz.bbs.thread.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.thread.ThreadPropertyPO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@Service
public class ThreadPropertyServiceImpl extends ServiceImpl<ThreadPropertyMapper, ThreadPropertyPO> implements IThreadPropertyService {
    @Override
    public boolean deleteByThreadId(Long threadId, int propertyType) {
        QueryWrapper<ThreadPropertyPO> queryWrapper = new QueryWrapper<ThreadPropertyPO>()
                .eq("thread_id", threadId).eq("property_type", propertyType);
        return remove(queryWrapper);
    }
}
