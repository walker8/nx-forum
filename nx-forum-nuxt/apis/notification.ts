import Http from '../utils/request'

export const getNotificationCount = () => {
  return Http.get(`/v1/notifications/count`)
}

export const queryNotifications = (type: string, pageNo: number, pageSize: number) => {
  return Http.get(`/v1/notifications`, { type, pageNo, pageSize })
}

export const clearNotifications = (type: string) => {
  return Http.put(`/v1/notifications/clear?type=${type}`)
}
