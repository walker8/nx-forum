/**
 * 评论领域模型（Comment Domain）
 * <p>
 * 本包包含评论的领域实体、值对象、领域服务、网关接口等。
 * 采用 DDD（领域驱动设计）模式，封装评论的核心业务逻辑。
 * </p>
 *
 * <h2>核心组件</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.content.comment.CommentE} - 评论领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.comment.CommentReplyE} - 回复领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.comment.service.CommentDomainService} - 评论领域服务</li>
 *   <li>{@link com.leyuz.bbs.content.comment.gateway.CommentGateway} - 评论网关接口</li>
 * </ul>
 *
 * <h2>领域事件</h2>
 * <p>评论模块通过事件机制实现与其他模块的解耦：</p>
 * <ul>
 *   <li>CommentNewEvent - 评论创建事件</li>
 *   <li>CommentPassedEvent - 评论审核通过事件</li>
 *   <li>CommentRejectedEvent - 评论审核拒绝事件</li>
 *   <li>CommentDeletedEvent - 评论删除事件</li>
 *   <li>CommentRestoredEvent - 评论恢复事件</li>
 *   <li>CommentReplyNewEvent - 回复创建事件</li>
 *   <li>CommentReplyPassedEvent - 回复审核通过事件</li>
 *   <li>CommentReplyRejectedEvent - 回复审核拒绝事件</li>
 *   <li>CommentReplyDeletedEvent - 回复删除事件</li>
 *   <li>CommentReplyRestoredEvent - 回复恢复事件</li>
 * </ul>
 *
 * @author walker
 * @since 2024-07-28
 */
package com.leyuz.bbs.content.comment;


