import Http from '../utils/request'

export const getAuditingCount = (forumId: number) => {
  return Http.get(`/v1/admin/posts/auditing/count?forumId=${forumId}`)
}
