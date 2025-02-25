package com.leyuz.bbs.content.thread.plugin;

import com.leyuz.bbs.content.thread.ThreadE;

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
} 