export default defineNuxtRouteMiddleware((to, from) => {
  if (import.meta.client) {
    const token = useCookie('x_token')
    if (token.value) {
      // 用户已登录
      if (to?.path) {
        if (
          to.path === '/uc/login' ||
          to.path === '/uc/register' ||
          to.path === '/uc/reset-password'
        ) {
          //return abortNavigation()
          return '/'
        }
      }
    } else {
      // 用户未登录
      if (to?.path) {
        if (
          to.path.startsWith('/uc/admin') ||
          to.path.startsWith('/uc/settings') ||
          to.path.startsWith('/admin') ||
          to.path.startsWith('/editor') ||
          to.path.startsWith('/notification')
        ) {
          //return abortNavigation()
          return '/uc/login'
        }
      }
    }
  }
})
