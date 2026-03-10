package com.leyuz.bbs.content.thread.plugin;

import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.dto.ThreadVO;

import java.util.Collections;
import java.util.List;

/**
 * 主题插件接口
 * 允许用户通过实现此接口来自定义主题保存和更新前的处理逻辑
 */
public interface ThreadPlugin {

    /**
     * 在保存主题前执行
     * 可以修改ThreadE的属性
     *
     * @param threadE 主题实体
     * @return 修改后的主题实体
     */
    void beforeSave(ThreadE threadE);

    /**
     * 在更新主题前执行
     * 可以修改ThreadE的属性
     *
     * @param threadE 主题实体
     * @return 修改后的主题实体
     */
    void beforeUpdate(ThreadE threadE);

    /**
     * 获取插件优先级
     * 数值越小优先级越高
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 在查询主题列表时获取额外的主题
     * 允许二次开发者在主题列表中动态插入/新增主题
     * 注意：返回的主题将添加到列表末尾，系统会自动过滤重复的threadId
     *
     * @param query 查询条件
     * @return 额外的主题列表，默认返回空列表
     */
    default List<ThreadVO> getAdditionalThreads(ThreadQuery query) {
        return Collections.emptyList();
    }
} 