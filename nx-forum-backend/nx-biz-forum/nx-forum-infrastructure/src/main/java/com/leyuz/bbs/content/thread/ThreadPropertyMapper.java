package com.leyuz.bbs.content.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyuz.bbs.content.thread.ThreadPropertyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@Mapper
public interface ThreadPropertyMapper extends BaseMapper<ThreadPropertyPO> {
    
    default boolean deleteByThreadId(Long threadId, int propertyType) {
        QueryWrapper<ThreadPropertyPO> queryWrapper = new QueryWrapper<ThreadPropertyPO>()
                .eq("thread_id", threadId).eq("property_type", propertyType);
        return delete(queryWrapper) > 0;
    }
}
