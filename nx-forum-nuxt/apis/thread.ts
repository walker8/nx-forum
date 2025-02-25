import type { ThreadCmd } from '~/types/global'
import Http from '../utils/request'

export type ThreadQuery = {
  forumName?: string
  forumId?: number
  authorName?: string
  keyword?: string
  ip?: string
  orderBy?: string
  pageNo?: number
  pageSize?: number
  auditStatus?: number
  propertyType?: number
  deleted?: boolean
}

export type HotThreadQuery = {
  days?: number
  limit?: number
}

export const getThreadForView = (threadId: number) => {
  return Http.get(`/v1/threads/view/${threadId}`)
}

export const getThreadForEdit = (threadId: number) => {
  return Http.get(`/v1/threads/edit/${threadId}`)
}

export const createThread = (thread: ThreadCmd) => {
  return Http.post(`/v1/threads`, thread)
}

export const updateThread = (threadId: number, thread: ThreadCmd) => {
  return Http.put(`/v1/threads/${threadId}`, thread)
}

export const queryThreads = (query: ThreadQuery) => {
  return Http.get(`/v1/threads`, query)
}

export const queryThreadsByKeyword = (query: ThreadQuery) => {
  return Http.post(`/v1/threads/search`, query)
}

export const queryThreadsByUserId = (userId: number, query: ThreadQuery) => {
  return Http.get(`/v1/users/${userId}/threads`, query)
}

export const queryThreadsByAdmin = (query: ThreadQuery) => {
  return Http.get(`/v1/admin/threads`, query)
}

export const operateThreadsByAdmin = (
  threadIds: number[],
  forumId: number,
  operation: string,
  reason: string | null = null,
  notice: boolean = true
) => {
  // Start with the base URL
  let url = `/v1/admin/threads/batch/operation?operation=${operation}&forumId=${forumId}&notice=${notice}`

  // Append reason if it is not null
  if (reason !== null) {
    url += `&reason=${encodeURIComponent(reason)}`
  }
  return Http.put(url, threadIds)
}

export const getHotPosts = (query?: HotThreadQuery) => {
  return Http.get('/v1/threads/hot', query)
}
