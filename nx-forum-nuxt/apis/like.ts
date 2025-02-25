import type { LikeCmd, Query } from '~/types/global'
import Http from '../utils/request'

export const toggleLike = (data: LikeCmd) => {
  return Http.post('/v1/users/like', data)
}

export const queryLikedThreadsByUserId = (userId: number, query: Query) => {
  return Http.get(`/v1/users/${userId}/likes`, query)
}
