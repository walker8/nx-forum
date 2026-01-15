import type { DeviceInfo, DeviceId } from '~/types/global'

/**
 * 设备 ID localStorage 存储键名
 * 使用项目前缀避免冲突
 */
const STORAGE_KEY = 'nx_device_id'

/**
 * 生成设备指纹
 * 基于浏览器基础特征生成 SHA-256 哈希
 *
 * 特征选择原则:
 * 1. 稳定性: 特征在会话间保持一致
 * 2. 隐私友好: 不使用已弃用 API (如 navigator.platform)
 * 3. 低熵: 不收集高精度信息 (如 Canvas 指纹)
 *
 * @returns Promise<string> 32位十六进制哈希字符串
 */
async function generateFingerprint(): Promise<string> {
  // 收集稳定的浏览器特征
  const features = [
    // 用户代理 (浏览器标识)
    navigator.userAgent,
    // 语言设置
    navigator.language,
    // 屏幕分辨率
    `${screen.width}x${screen.height}`,
    // 颜色深度
    screen.colorDepth,
    // 时区偏移 (分钟)
    new Date().getTimezoneOffset().toString(),
    // 触摸点数量 (区分移动/桌面)
    navigator.maxTouchPoints?.toString() || '0',
    // 触摸支持
    'ontouchstart' in window ? '1' : '0',
    // WebGL 支持 (可选,用于增强区分度)
    (() => {
      try {
        const canvas = document.createElement('canvas')
        const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
        if (gl && gl instanceof WebGLRenderingContext) {
          return 'webgl-supported'
        }
      } catch (e) {
        // WebGL 不可用或被禁用
      }
      return 'no-webgl'
    })()
  ].join('|')

  // 使用 SHA-256 生成哈希
  const hashBuffer = await crypto.subtle.digest(
    'SHA-256',
    new TextEncoder().encode(features)
  )

  // 转换为十六进制字符串,取前 32 位
  const hashHex = Array.from(new Uint8Array(hashBuffer))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
    .substring(0, 32)

  return hashHex
}

/**
 * 从 localStorage 获取或生成设备 ID
 *
 * 逻辑流程:
 * 1. 检查 localStorage 是否存在设备 ID
 * 2. 如果存在,直接返回
 * 3. 如果不存在,生成新的设备 ID 并存储
 * 4. 如果生成失败 (如 crypto API 不可用),生成随机 ID
 *
 * @returns Promise<DeviceId> 设备 ID
 */
async function getOrGenerateDeviceId(): Promise<DeviceId> {
  // 仅在客户端执行
  if (import.meta.server) {
    return ''
  }

  try {
    // 尝试从 localStorage 读取
    const storedId = localStorage.getItem(STORAGE_KEY)
    if (storedId && storedId.length > 0) {
      return storedId
    }

    // 生成新的设备 ID
    let deviceId = ''

    try {
      // 优先使用指纹生成
      deviceId = await generateFingerprint()
    } catch (error) {
      // 降级方案: 生成随机 ID
      console.warn('Failed to generate device fingerprint, using random ID:', error)
      const array = new Uint8Array(16)
      crypto.getRandomValues(array)
      deviceId = Array.from(array)
        .map((b) => b.toString(16).padStart(2, '0'))
        .join('')
        .substring(0, 32)
    }

    // 存储到 localStorage
    localStorage.setItem(STORAGE_KEY, deviceId)

    return deviceId
  } catch (error) {
    console.error('Failed to get or generate device ID:', error)
    return ''
  }
}

/**
 * 设备管理 Composable
 *
 * 提供设备 ID 的初始化、访问和管理功能
 *
 * 使用示例:
 * ```ts
 * const { deviceId, deviceInfo, initDevice, isReady } = useDevice()
 * await initDevice()
 * console.log('Device ID:', deviceId.value)
 * ```
 */
export const useDevice = () => {
  // 使用 useState 实现跨组件状态共享
  // Key 命名规范: 使用 'device:' 前缀避免冲突
  const deviceId = useState<DeviceId>('device:id', () => '')
  const deviceInfo = useState<DeviceInfo | null>('device:info', () => null)
  const isReady = ref<boolean>(false)

  /**
   * 初始化设备 ID
   * 在客户端首次调用时生成并存储设备 ID
   *
   * SSR 兼容性:
   * - 服务端直接返回,不执行任何操作
   * - 客户端在首次调用时初始化
   */
  const initDevice = async () => {
    // SSR 保护: 仅在客户端执行
    if (import.meta.server) {
      return
    }

    // 避免重复初始化
    if (isReady.value && deviceId.value) {
      return
    }

    try {
      // 获取或生成设备 ID
      const id = await getOrGenerateDeviceId()

      // 更新响应式状态
      deviceId.value = id
      deviceInfo.value = {
        deviceId: id,
        generatedAt: Date.now(),
        fingerprintData: 'SHA-256 hash'
      }

      // 标记初始化完成
      isReady.value = true
    } catch (error) {
      console.error('Failed to initialize device:', error)
      isReady.value = true // 即使失败也标记为就绪,避免重复尝试
    }
  }

  /**
   * 获取当前设备 ID (同步)
   * 用于在已初始化后快速访问
   *
   * @returns DeviceId 当前设备 ID,未初始化返回空字符串
   */
  const getDeviceId = (): DeviceId => {
    return deviceId.value || ''
  }

  /**
   * 重置设备 ID
   * 清除 localStorage 并重新生成
   *
   * 注意: 此功能目前未暴露给用户,仅用于调试
   */
  const resetDevice = async () => {
    if (import.meta.server) {
      return
    }

    try {
      localStorage.removeItem(STORAGE_KEY)
      deviceId.value = ''
      deviceInfo.value = null
      isReady.value = false
      await initDevice()
    } catch (error) {
      console.error('Failed to reset device:', error)
    }
  }

  return {
    // 只读状态,防止外部直接修改
    deviceId: readonly(deviceId),
    deviceInfo: readonly(deviceInfo),
    isReady: readonly(isReady),
    // 方法
    initDevice,
    getDeviceId,
    resetDevice
  }
}

/**
 * 获取客户端设备 ID (工具函数)
 *
 * 这是一个同步函数,用于在非响应式上下文中快速获取设备 ID
 * 主要用于请求拦截器等场景
 *
 * @returns DeviceId 当前设备 ID,未初始化返回空字符串
 */
export function getClientDeviceId(): DeviceId {
  if (import.meta.client) {
    try {
      return localStorage.getItem(STORAGE_KEY) || ''
    } catch (error) {
      console.error('Failed to get device ID from localStorage:', error)
      return ''
    }
  }
  return ''
}
