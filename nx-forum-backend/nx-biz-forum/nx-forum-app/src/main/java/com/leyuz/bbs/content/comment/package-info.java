/**
 * 评论管理模块（Comment Management）
 * <p>
 * 本模块负责论坛评论和回复的核心业务逻辑。采用 DDD（领域驱动设计）模式开发。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>评论发布：支持对帖子的评论</li>
 *   <li>评论回复：支持对评论的回复和嵌套回复</li>
 *   <li>评论查询：支持分页查询、排序等</li>
 *   <li>评论审核：自动审核和人工审核</li>
 *   <li>评论管理：删除、恢复等管理操作</li>
 * </ul>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.content.comment.CommentApplication} - 评论应用服务</li>
 *   <li>{@link com.leyuz.bbs.content.comment.CommentE} - 评论领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.comment.CommentReplyE} - 回复领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.comment.service.CommentDomainService} - 评论领域服务</li>
 *   <li>{@link com.leyuz.bbs.content.comment.gateway.CommentGateway} - 评论网关接口</li>
 * </ul>
 *
 * <h2>事件驱动</h2>
 * <p>评论模块通过事件机制与其他模块解耦，支持评论创建、审核、删除等事件通知。</p>
 *
 * @author walker
 * @since 2024-07-28
 */
package com.leyuz.bbs.content.comment;


