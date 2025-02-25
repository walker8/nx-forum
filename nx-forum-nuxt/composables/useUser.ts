import { ElMessage } from 'element-plus'
import { getCurrentUser } from '~/apis/uc/user'
import { getAuthConfig, logout, type AuthConfigVO } from '~/apis/uc/login'
import type { ForumMenuItemVO, UserVO } from '~/types/global'
import { queryPermissions } from '~/apis/auth'

export const useUser = () =>
  useState<UserVO>('user', () => ({
    userId: 0,
    avatar: '',
    userName: '',
    intro: '',
    createTime: '',
    followed: false,
    lastActiveDate: ''
  }))

export const useUserSettings = () =>
  useState('userSettings', () => ({
    selected: 'profile',
    settings: [
      {
        value: 'profile',
        label: '个人资料'
      },
      {
        value: 'account',
        label: '账号设置'
      }
    ]
  }))

export const useUserHome = () =>
  useState('userHome', () => ({
    selected: '',
    tabs: [
      {
        value: '',
        label: '文章',
        icon: 'tabler:article'
      },
      {
        value: 'comment',
        label: '评论',
        icon: 'tabler:message'
      },
      {
        value: 'favorite',
        label: '收藏',
        icon: 'tabler:bookmark'
      },
      {
        value: 'following',
        label: '关注',
        icon: 'tabler:user-plus'
      },
      {
        value: 'followers',
        label: '粉丝',
        icon: 'tabler:users'
      },
      {
        value: 'like',
        label: '点赞',
        icon: 'tabler:thumb-up'
      }
    ]
  }))

export const useUserNotification = () =>
  useState('userNotification', () => ({
    selected: 'reply',
    tabs: [
      {
        value: 'reply',
        label: '评论消息',
        count: 'replyCount'
      },
      {
        value: 'mention',
        label: '@提醒',
        count: 'mentionCount'
      },
      {
        value: 'system',
        label: '系统消息',
        count: 'systemCount'
      }
    ]
  }))

export const useUserNotificationCount = () =>
  useState('userNotificationCount', () => ({
    replyCount: 0,
    systemCount: 0,
    mentionCount: 0,
    totalCount: 0
  }))

export const useUserMenus = () => useState<ForumMenuItemVO[]>('userMenus', () => [])

export const useUserInfo = () =>
  useState<UserVO>('userInfo', () => ({
    userId: 0,
    userName: '',
    avatar: '',
    intro: '',
    createTime: '',
    followed: false,
    lastActiveDate: ''
  }))

let fetchPromise: Promise<void> | null = null
export function useCurrentUser() {
  const user = useUser()

  if (import.meta.client && !(user.value.userId && user.value.userId > 0)) {
    // 只在客户端执行代码 并且 userId 没有赋值
    nextTick(() => {
      const token = useCookie('x_token')
      if (token.value && !fetchPromise) {
        fetchPromise = getCurrentUser()
          .then((res) => {
            const data = res.data
            if (!data) {
              token.value = ''
            } else {
              user.value = { ...user.value, ...data }
              user.value.avatar = data.avatar || '/img/avatar.png'
            }
          })
          .finally(() => {
            fetchPromise = null
          })
      }
    })
  }

  const handleLogout = () => {
    showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
      .then(async () => {
        // 用户点击确认
        try {
          await logout()
          // 清空用户信息
          user.value = {
            userId: 0,
            avatar: '',
            userName: '',
            intro: '',
            createTime: '',
            followed: false,
            lastActiveDate: ''
          }
          // 清除token
          const token = useCookie('x_token')
          token.value = ''
          // 清除用户权限
          const { refreshUserAuth, authPromise } = useUserAuth()
          await authPromise
          refreshUserAuth()
          ElMessage.success('退出登录成功')
          // 跳转到首页
          navigateTo('/')
        } catch (error) {
          ElMessage.error('退出登录失败')
        }
      })
      .catch(() => {
        // 用户点击取消
      })
  }
  return { user, handleLogout }
}

export const useAuthConfig = async () => {
  const authConfig = useState<AuthConfigVO>('authConfig', () => ({
    loginConfig: undefined,
    registerConfig: undefined
  }))
  if (import.meta.client && !authConfig.value.loginConfig) {
    // 只在客户端执行代码 并且 loginConfig没有赋值
    await nextTick()
    let res = await getAuthConfig()
    authConfig.value = res.data
  }
  return authConfig
}

/**
 * 获取权限，仅在客户端执行（onMounted）
 * @param forumId 版块id
 * @returns
 */
const permissionFetchPromises = new Map<number, Promise<any>>()
export const useUserAuth = (forumId: number = 0) => {
  // 服务端直接返回空实现，避免在 SSR 阶段发起权限接口请求
  if (import.meta.server) {
    const userAuth = useState('userAuth', () => ({
      permissions: ref<Record<number, string[]>>({}),
      loadingStates: ref<Record<number, boolean>>({})
    }))
    const hasPermission = () => () => false
    const refreshUserAuth = () => { }
    const authPromise = Promise.resolve()
    const isLoading = ref(false)
    return { userAuth, hasPermission, refreshUserAuth, authPromise, isLoading }
  }

  const userAuth = useState('userAuth', () => ({
    permissions: ref<Record<number, string[]>>({}),
    loadingStates: ref<Record<number, boolean>>({})
  }))

  // 确保 permissions 始终存在（处理从服务端恢复状态的情况）
  if (!userAuth.value.permissions) {
    userAuth.value.permissions = ref<Record<number, string[]>>({})
  }
  // 确保 permissions.value 已初始化
  if (!userAuth.value.permissions.value) {
    userAuth.value.permissions.value = {}
  }
  // 确保 loadingStates 存在并初始化
  if (!userAuth.value.loadingStates) {
    userAuth.value.loadingStates = ref<Record<number, boolean>>({})
  }
  if (!userAuth.value.loadingStates.value) {
    userAuth.value.loadingStates.value = {}
  }

  const getUserAuth = async () => {
    // 获取用户权限
    if (!forumId || forumId < 10) {
      // 10 以下为系统板块
      forumId = 0
    }

    // 确保 permissions.value 已初始化
    if (!userAuth.value.permissions.value) {
      userAuth.value.permissions.value = {}
    }

    // 如果已经有该 forumId 的权限数据,直接返回
    if (userAuth.value.permissions.value[forumId] && userAuth.value.permissions.value[forumId].length > 0) {
      return
    }

    if (permissionFetchPromises.has(forumId)) {
      await permissionFetchPromises.get(forumId)
      return
    }

    // 设置加载状态为 true
    userAuth.value.loadingStates.value[forumId] = true

    const promise = queryPermissions(forumId)
      .then((res) => {
        // 使用响应式赋值,触发Vue更新
        userAuth.value.permissions.value[forumId] = res.data
      })
      .catch((error) => {
        // 即使失败也要设置一个空数组，避免重复请求
        userAuth.value.permissions.value[forumId] = []
      })
      .finally(() => {
        // 设置加载状态为 false
        userAuth.value.loadingStates.value[forumId] = false
        permissionFetchPromises.delete(forumId)
      })

    permissionFetchPromises.set(forumId, promise)
    await promise
  }

  // 启动权限获取,并暴露Promise供外部等待
  const authPromise = getUserAuth()
  // 捕获错误避免未处理的Promise rejection
  authPromise.catch(() => { })

  // 标准化 forumId
  let normalizedForumId = forumId
  if (!normalizedForumId || normalizedForumId < 10) {
    normalizedForumId = 0
  }

  // 返回该 forumId 的加载状态
  const isLoading = computed(() => userAuth.value.loadingStates.value[normalizedForumId] ?? false)

  /**
   * 检查是否有指定权限
   * @param permission 权限标识
   * @param checkForumId 版块ID
   * @returns
   */
  const hasPermission = (permission: string, checkForumId: number = 0) => {
    let forumId = checkForumId
    if (!forumId || forumId < 10) {
      // 10 以下为系统板块
      forumId = 0
    }
    if (!userAuth.value.permissions) {
      return false
    }
    return userAuth.value.permissions.value?.[forumId]?.includes(permission) ?? false
  }

  const refreshUserAuth = () => {
    // 确保 permissions 存在
    if (!userAuth.value.permissions) {
      userAuth.value.permissions = ref<Record<number, string[]>>({})
    }
    // 清空权限对象
    userAuth.value.permissions.value = {}
    getUserAuth()
  }
  return { userAuth, hasPermission, refreshUserAuth, authPromise, isLoading }
}
