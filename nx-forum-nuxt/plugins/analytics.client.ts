/**
 * 统计代码注入插件
 * 仅在客户端执行，在应用启动时注入统计代码
 * 使用 onMounted 确保在 hydration 完成后执行，避免 hydration mismatch
 * 排除后台管理页面（/admin/**）
 */
export default defineNuxtPlugin({
  name: 'analytics',
  setup(nuxtApp) {
    // 插件已经是 .client.ts，所以这里肯定在客户端
    // 但为了安全，还是检查一下
    if (import.meta.server) {
      return
    }

    // 使用 Vue 的 onMounted 钩子，确保在组件挂载后执行
    // 这样可以避免在 hydration 期间执行，防止不匹配
    nuxtApp.hook('app:mounted', () => {
      // 检查当前路由，排除后台管理页面
      const route = useRoute()
      if (route.path.startsWith('/admin')) {
        return
      }

      // 在应用完全挂载后再注入统计代码
      // 使用 setTimeout 进一步延迟，确保所有 hydration 都已完成
      setTimeout(() => {
        const { injectAnalyticsCode } = useAnalytics()
        injectAnalyticsCode()
      }, 200) // 延迟 200ms 确保 hydration 完全完成
    })
  }
})

