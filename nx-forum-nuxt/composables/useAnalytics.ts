import { getAnalyticsCode } from '~/apis/config'

/**
 * 统计代码管理 composable
 * 用于获取和注入统计代码
 */
export const useAnalytics = () => {
  // 使用 useState 缓存统计代码，避免重复请求
  const analyticsCode = useState<string | null>('analytics:code', () => null)

  /**
   * 获取统计代码
   */
  const fetchAnalyticsCode = async (): Promise<string | null> => {
    // 如果已经缓存，直接返回
    if (analyticsCode.value !== null) {
      return analyticsCode.value
    }

    try {
      const res = await getAnalyticsCode()
      const code: string = res.data || ''
      
      // 缓存结果（包括空字符串）
      analyticsCode.value = code || null
      
      return analyticsCode.value
    } catch (error) {
      console.error('获取统计配置失败:', error)
      analyticsCode.value = null
      return null
    }
  }

  /**
   * 注入统计代码到页面
   * 使用直接 DOM 操作，确保在 hydration 完成后执行，避免 SSR 不匹配
   */
  const injectAnalyticsCode = async () => {
    // 确保仅在客户端执行
    if (import.meta.server) {
      return
    }

    // 确保在浏览器环境中
    if (typeof window === 'undefined' || typeof document === 'undefined') {
      return
    }

    // 等待 hydration 完成 - 检查 DOM 是否已完全加载
    await nextTick()
    
    // 额外等待，确保 hydration 完全完成
    // 使用 requestAnimationFrame 确保在浏览器渲染完成后执行
    await new Promise(resolve => {
      if (typeof requestAnimationFrame !== 'undefined') {
        requestAnimationFrame(() => {
          setTimeout(resolve, 50) // 再延迟 50ms 确保 hydration 完成
        })
      } else {
        setTimeout(resolve, 100)
      }
    })

    const code = await fetchAnalyticsCode()
    
    if (!code || code.trim() === '') {
      return
    }

    // 检查是否已经注入过（避免重复注入）
    if (document.querySelector('script[data-analytics-injected="true"]')) {
      return
    }

    try {
      // 如果代码包含 <script> 标签，提取内容
      let scriptContent = code.trim()
      if (scriptContent.includes('<script')) {
        // 提取 script 标签内的内容
        const match = scriptContent.match(/<script[^>]*>([\s\S]*?)<\/script>/i)
        if (match && match[1]) {
          scriptContent = match[1].trim()
        } else {
          // 如果没有匹配到，尝试提取所有 script 标签内容
          scriptContent = scriptContent.replace(/<script[^>]*>/gi, '').replace(/<\/script>/gi, '')
        }
      }

      // 直接使用 DOM 操作注入脚本，避免 useHead 在 hydration 期间的副作用
      // 这种方式在 hydration 完成后执行，不会影响 SSR
      const script = document.createElement('script')
      script.setAttribute('data-analytics-injected', 'true')
      script.type = 'text/javascript'
      script.textContent = scriptContent
      
      // 插入到 head 中
      const head = document.head || document.getElementsByTagName('head')[0]
      if (head) {
        head.appendChild(script)
      }
    } catch (error) {
      console.error('注入统计代码失败:', error)
    }
  }

  /**
   * 清除缓存的统计代码（用于配置更新后刷新）
   */
  const clearCache = () => {
    analyticsCode.value = null
  }

  return {
    analyticsCode: readonly(analyticsCode),
    fetchAnalyticsCode,
    injectAnalyticsCode,
    clearCache
  }
}

