import type { Query } from '~/types/global'
import Http from '../utils/request'

export interface FavoriteCmd {
  threadId: number
  isFavorite: boolean
}

export const toggleFavorite = (data: FavoriteCmd) => {
  return Http.post('/v1/users/favorite', data)
}

export const queryFavoritesByUserId = (userId: number, query: Query) => {
  return Http.get(`/v1/users/${userId}/favorites`, { params: query })
} 