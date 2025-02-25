/**
 * 帖子领域模型（Thread Domain）
 * <p>
 * 本包包含帖子的领域实体、值对象、领域服务、网关接口等。
 * 采用 DDD（领域驱动设计）模式，封装帖子的核心业务逻辑。
 * </p>
 *
 * <h2>核心组件</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.content.thread.ThreadE} - 帖子领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.thread.ThreadPropertyE} - 帖子属性实体</li>
 *   <li>{@link com.leyuz.bbs.content.thread.service.ThreadDomainService} - 帖子领域服务</li>
 *   <li>{@link com.leyuz.bbs.content.thread.gateway.ThreadGateway} - 帖子网关接口</li>
 * </ul>
 *
 * <h2>领域事件</h2>
 * <p>帖子模块通过事件机制实现与其他模块的解耦：</p>
 * <ul>
 *   <li>ThreadNewEvent - 帖子创建事件</li>
 *   <li>ThreadUpdateEvent - 帖子更新事件</li>
 *   <li>ThreadDeletedEvent - 帖子删除事件</li>
 *   <li>ThreadPassedEvent - 帖子审核通过事件</li>
 *   <li>ThreadRejectedEvent - 帖子审核拒绝事件</li>
 *   <li>ThreadRestoredEvent - 帖子恢复事件</li>
 *   <li>ThreadTransferredEvent - 帖子转移事件</li>
 *   <li>ThreadChangePropertyEvent - 帖子属性变更事件</li>
 * </ul>
 *
 * @author walker
 * @since 2024-07-07
 */
package com.leyuz.bbs.content.thread;


