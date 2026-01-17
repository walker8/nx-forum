import { computed, onMounted, onUnmounted, ref, type Ref, watch } from 'vue'
import { useWindowScroll } from '@vueuse/core'

export interface UseAutoLoadMoreOptions {
  disabled: Ref<boolean>      // 加载禁用状态
  hasMore: Ref<boolean>       // 是否有更多数据
  onLoad: () => void          // 加载回调函数
  distance?: number           // 触发距离（默认 200px）
  mobileOnly?: boolean        // 仅移动端启用（默认 true）
}

export function useAutoLoadMore(options: UseAutoLoadMoreOptions) {
  const {
    disabled,
    hasMore,
    onLoad,
    distance = 200,
    mobileOnly = true
  } = options

  const isMobile = ref(false)
  const isLoading = ref(false)

  // 移动端检测
  const checkMobile = () => {
    if (import.meta.client) {
      isMobile.value = window.innerWidth < 768
    }
  }

  // 滚动监听
  const { y: scrollTop } = useWindowScroll()

  const isNearBottom = computed(() => {
    if (!import.meta.client) return false
    const scrollHeight = document.documentElement.scrollHeight
    const clientHeight = window.innerHeight
    return scrollHeight - scrollTop.value - clientHeight <= distance
  })

  // 自动触发加载
  watch([isNearBottom, isMobile], ([nearBottom, mobile]) => {
    if (mobileOnly && !mobile) return
    if (nearBottom && !disabled.value && hasMore.value && !isLoading.value) {
      isLoading.value = true
      try {
        onLoad()
      } finally {
        // 等待外部加载完成后重置
        setTimeout(() => {
          isLoading.value = false
        }, 100)
      }
    }
  })

  // 初始化和监听窗口变化
  onMounted(() => {
    if (import.meta.client) {
      checkMobile()
      window.addEventListener('resize', checkMobile)
    }
  })

  onUnmounted(() => {
    if (import.meta.client) {
      window.removeEventListener('resize', checkMobile)
    }
  })

  return {
    isMobile,
    isLoading
  }
}
