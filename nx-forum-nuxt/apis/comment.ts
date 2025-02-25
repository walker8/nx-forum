import type { Query } from '~/types/global'
import Http from '../utils/request'

export type CommentQuery = {
  threadId?: number
  ip?: string
  forumId?: number
  authorName?: string
  sort?: number
  pageNo: number
  pageSize: number
  auditStatus?: number
  deleted?: boolean
}
export type CommentReplyQuery = {
  ip?: string
  forumId?: number
  authorName?: string
  pageNo: number
  pageSize: number
  auditStatus?: number
  deleted?: boolean
}
/**
 * 查询帖子下的评论
 * @param threadId
 * @param order
 * @param pageNo
 * @param pageSize
 * @param commentId 指定评论ID
 * @returns
 */
export const queryComments = (
  threadId: number,
  order: number,
  pageNo: number,
  pageSize: number,
  commentId?: number
) => {
  return Http.get(`/v1/comments`, {
    threadId,
    order,
    pageNo,
    pageSize,
    commentId
  })
}

export const queryCommentsByUserId = (userId: number, query: Query) => {
  return Http.get(`/v1/users/${userId}/comments`, query)
}

export const queryCommentReplies = (
  commentId: number,
  order: number,
  pageNo: number,
  pageSize: number,
  replyId?: number
) => {
  return Http.get(`/v1/comments/${commentId}/replies`, {
    commentId,
    order,
    pageNo,
    pageSize,
    replyId
  })
}

export const getCommentVOById = (commentId: number, replyId?: number) => {
  return Http.get(`/v1/comments/${commentId}`, {
    replyId
  })
}

/**
 * 新增评论的回复
 * @param commentId
 * @param replyUserId
 * @returns
 */
export const createCommentReply = (commentId: number, message: string, replyUserId: number) => {
  return Http.post(`/v1/comments/${commentId}/replies`, {
    message,
    replyUserId
  })
}

/**
 * 新增评论
 * @param threadId
 * @param message
 * @param images
 * @returns
 */
export const createComment = (threadId: number, message: string, images: Array<string>) => {
  return Http.post(`/v1/comments`, {
    threadId,
    message,
    images
  })
}

export const queryCommentsByAdmin = (commentQuery: CommentQuery) => {
  return Http.get(`/v1/admin/comments`, commentQuery)
}

export const queryCommentRepliesByAdmin = (commentReplyQuery: CommentReplyQuery) => {
  return Http.get(`/v1/admin/comments/replies`, commentReplyQuery)
}

export const deleteCommentsByAdmin = (
  commentIds: number[],
  forumId: number,
  reason: string | null,
  notice = true
) => {
  const params = new URLSearchParams({
    operation: 'delete',
    forumId: forumId.toString(),
    notice: notice.toString()
  })
  if (reason) params.append('reason', reason)

  return Http.put(`/v1/admin/comments/batch/operation?${params}`, commentIds)
}
export const passCommentsByAdmin = (commentIds: number[], forumId: number, notice = true) => {
  return Http.put(
    `/v1/admin/comments/batch/operation?operation=pass&forumId=${forumId}&notice=${notice}`,
    commentIds
  )
}
export const rejectCommentsByAdmin = (
  commentIds: number[],
  forumId: number,
  reason: string | null,
  notice = true
) => {
  const params = new URLSearchParams({
    operation: 'reject',
    forumId: forumId.toString(),
    notice: notice.toString()
  })
  if (reason) params.append('reason', reason)

  return Http.put(`/v1/admin/comments/batch/operation?${params}`, commentIds)
}
export const restoreCommentsByAdmin = (commentIds: number[], forumId: number, notice: boolean) => {
  return Http.put(
    `/v1/admin/comments/batch/operation?operation=restore&forumId=${forumId}&notice=${notice}`,
    commentIds
  )
}

export const deleteCommentRepliesByAdmin = (
  replyIds: number[],
  forumId: number,
  reason: string | null,
  notice = true
) => {
  const params = new URLSearchParams({
    operation: 'delete',
    forumId: forumId.toString(),
    notice: notice.toString()
  })
  if (reason) params.append('reason', reason)
  return Http.put(`/v1/admin/comments/replies/batch/operation?${params}`, replyIds)
}
export const passCommentRepliesByAdmin = (replyIds: number[], forumId: number, notice = true) => {
  return Http.put(
    `/v1/admin/comments/replies/batch/operation?operation=pass&forumId=${forumId}&notice=${notice}`,
    replyIds
  )
}
export const rejectCommentRepliesByAdmin = (
  replyIds: number[],
  forumId: number,
  reason: string | null,
  notice = true
) => {
  const params = new URLSearchParams({
    operation: 'reject',
    forumId: forumId.toString(),
    notice: notice.toString()
  })
  if (reason) params.append('reason', reason)

  return Http.put(`/v1/admin/comments/replies/batch/operation?${params}`, replyIds)
}
export const restoreCommentRepliesByAdmin = (
  replyIds: number[],
  forumId: number,
  notice = true
) => {
  return Http.put(
    `/v1/admin/comments/replies/batch/operation?operation=restore&forumId=${forumId}&notice=${notice}`,
    replyIds
  )
}
