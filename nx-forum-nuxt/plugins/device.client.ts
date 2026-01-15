/**
 * 设备 ID 初始化插件
 * 仅在客户端执行,在应用启动时初始化设备 ID
 *
 * 设计思路:
 * 1. 使用 .client.ts 后缀确保仅在客户端加载
 * 2. 在 app:mounted 钩子中初始化,确保 DOM 已加载
 * 3. 使用 setTimeout 延迟执行,避免阻塞首屏渲染
 * 4. 参考 analytics.client.ts 的实现模式
 */
export default defineNuxtPlugin({
  name: 'device',
  setup(nuxtApp) {
    // 双重保护: 虽然是 .client.ts,但仍然检查
    if (import.meta.server) {
      return
    }

    // 在应用挂载后初始化设备 ID
    // 这确保了:
    // 1. DOM 已加载 (可以访问 navigator, screen 等)
    // 2. SSR hydration 已完成 (不会导致状态不匹配)
    // 3. 不阻塞首屏渲染
    nuxtApp.hook('app:mounted', () => {
      // 使用 requestAnimationFrame 确保在浏览器渲染循环中执行
      requestAnimationFrame(() => {
        // 延迟 50ms,确保所有 hydration 完成
        // 参考 analytics.client.ts 的实现
        setTimeout(async () => {
          try {
            const { initDevice } = useDevice()
            await initDevice()

            // 开发环境下打印设备 ID (生产环境可移除)
            if (import.meta.dev) {
              const { deviceId } = useDevice()
              console.log('[Device Plugin] Initialized with ID:', deviceId.value)
            }
          } catch (error) {
            console.error('[Device Plugin] Failed to initialize device:', error)
          }
        }, 50)
      })
    })

    // 可选: 在页面路由变化时验证设备 ID
    // 这可以检测到 localStorage 被清除的情况
    nuxtApp.hook('page:finish', () => {
      const { deviceId, isReady, initDevice } = useDevice()

      // 如果设备 ID 丢失且未在初始化中,重新初始化
      if (isReady.value && !deviceId.value) {
        console.warn('[Device Plugin] Device ID lost, re-initializing...')
        initDevice()
      }
    })
  }
})
