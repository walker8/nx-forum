import { getNotificationCount } from '~/apis/notification'

/**
 * 通知数量获取 composable
 * 监听用户登录状态，自动获取通知数量
 * 内置去重机制，多个组件调用不会产生重复请求
 */
export function useInitNotificationCount() {
  const user = useUser()
  const userNotification = useUserNotificationCount()
  // 请求去重：防止多个组件并发调用时重复请求接口
  const fetchPromise = useState<Promise<void> | null>('notificationCountFetchPromise', () => null)
  // watcher 单例标志：确保多个组件调用时只注册一个 watcher
  const watcherRegistered = useState('notificationCountWatcherRegistered', () => false)

  /**
   * 获取通知数量
   * 通过 fetchPromise 实现去重，同一时刻只会有一个请求
   */
  const fetchNotificationCount = async () => {
    if (fetchPromise.value) {
      return fetchPromise.value
    }

    fetchPromise.value = getNotificationCount()
      .then((res) => {
        const data = res.data
        if (data) {
          userNotification.value = {
            mentionCount: data.mentionCount,
            replyCount: data.replyCount,
            systemCount: data.systemCount,
            totalCount: data.totalCount,
            totalAuditCount: data.totalAuditCount ?? null
          }
        }
      })
      .catch((error) => {
        console.error('获取通知数量失败:', error)
      })
      .finally(() => {
        fetchPromise.value = null
      })

    return fetchPromise.value
  }

  // 仅在客户端执行，且通过 watcherRegistered 保证 watcher 只注册一次
  if (import.meta.client && !watcherRegistered.value) {
    watcherRegistered.value = true
    watch(
      () => user.value.userId,
      (userId) => {
        if (userId && userId > 0) {
          fetchNotificationCount()
        } else {
          // 用户未登录或登出时，清空通知计数
          userNotification.value = {
            mentionCount: 0,
            replyCount: 0,
            systemCount: 0,
            totalCount: 0,
            totalAuditCount: null
          }
        }
      },
      { immediate: true }
    )
  }
}
