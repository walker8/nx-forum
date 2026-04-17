import type { _AsyncData } from '#app/composables/asyncData'
import { getClientDeviceId } from '~/composables/useDevice'

// 后端接口基础路径，所有请求 URL 会拼接此前缀
const baseUrl = '/nx-forum'

// 指定后端返回的基本数据类型
export interface ResponseConfig {
  success: boolean
  data: any
  errCode: string
  errMessage: string
}
export interface ValueConfig {
  value: any
}

/**
 * 生成稳定的缓存 key，用于 SSR payload 数据复用
 *
 * 在 Nuxt SSR 中，服务端通过 useFetch 请求数据后，会将结果存入 payload 传递给客户端。
 * 客户端在 hydration（水合）阶段可以通过同一 key 从 payload 中读取缓存数据，
 * 避免重复请求后端 API，从而防止因两次请求返回不同数据导致的 hydration mismatch。
 *
 * key 格式示例: "$api:get:/v1/threads/view/248" 或 "$api:get:/v1/threads:{"forumId":1}"
 */
function generateKey(method: string, url: string, params?: any, body?: any): string {
  const parts = [`$api:${method}:${url}`]
  if (params && typeof params === 'object' && Object.keys(params).length > 0) {
    parts.push(JSON.stringify(params))
  }
  if (body !== undefined) {
    parts.push(JSON.stringify(body))
  }
  return parts.join(':')
}

/**
 * 统一请求函数，封装了服务端和客户端两种请求策略:
 *
 * - 服务端: 使用 Nuxt 的 useFetch，支持 SSR 数据预取，结果自动存入 payload 传递给客户端
 * - 客户端 hydration 阶段: 直接复用 payload 中的缓存数据，不发起新请求
 * - 客户端正常导航阶段: 使用 $fetch 发起真实请求
 */
const fetch = async (url: string, options?: any): Promise<any> => {
  // 拼接完整请求路径: /nx-forum + /v1/threads/view/248
  const reqUrl = baseUrl + url
  const method = options?.method || 'get'
  // 生成与请求参数对应的唯一 key，确保服务端和客户端能匹配到同一条缓存
  const key = generateKey(method, url, options?.params, options?.body)

  let headers = {}
  if (import.meta.server) {
    // 服务端渲染时，需要将浏览器发来的原始请求头（主要是 cookie）透传给后端 API，
    // 否则后端无法识别当前登录用户
    headers = useRequestHeaders()
  } else {
    // 客户端注入设备 ID，用于设备去重和统计（不参与终端类型判断）
    const deviceId = getClientDeviceId()
    if (deviceId) {
      headers = { 'X-Device-Id': deviceId }
    }
  }
  return new Promise((resolve, reject) => {
    if (import.meta.client) {
      // === 客户端分支 ===

      // hydration 是指客户端接管服务端渲染的 HTML 并 "激活" 为响应式 Vue 应用的过程
      // 此阶段 nuxtApp.isHydrating 为 true，可以安全读取服务端预取的 payload 数据
      const nuxtApp = useNuxtApp()
      if (nuxtApp.isHydrating) {
        const cached = nuxtApp.payload.data?.[key]
        if (cached !== undefined) {
          // 命中缓存: 直接使用服务端已获取的数据，避免重复请求
          // 这是解决 hydration mismatch（服务端和客户端数据不一致）的关键
          if (cached && cached.success) {
            resolve(cached)
          } else {
            reject(cached?.errMessage || '获取数据失败')
          }
          return
        }
      }
      // 未命中缓存（非 hydration 阶段或 payload 中无对应数据）: 正常发起 HTTP 请求
      $fetch(reqUrl, {
        ...options,
        headers
      })
        .then((res: any) => {
          if (res.success) {
            resolve(res)
          } else {
            reject(res.errMessage)
          }
        })
        .catch((err) => {
          if (err.status === 401) {
            reject('当前用户无操作权限')
          } else {
            reject('服务器内部错误')
          }
        })
    } else {
      // === 服务端分支 ===

      // 使用 Nuxt 的 useFetch 而非原生 $fetch，因为:
      // 1. useFetch 会将请求结果自动存入 SSR payload，供客户端 hydration 复用
      // 2. 通过显式 key 参数确保缓存键稳定，不被自动生成的 key 干扰
      useFetch(reqUrl, {
        ...options,
        headers,
        key
      })
        .then(({ data, error }: _AsyncData<any, any>) => {
          if (error.value) {
            console.error('Request Error', error.value)
            if (error.value.statusCode === 401) {
              reject('当前用户无操作权限')
            } else {
              reject('服务器内部错误')
            }
            return
          }
          const res = data.value
          if (!res) {
            reject('服务器返回数据为空')
            return
          } else if (!res.success) {
            reject(res.errMessage)
          } else {
            resolve(res)
          }
        })
        .catch((err: any) => {
          reject(err)
        })
    }
  })
}

function isObject(obj: any) {
  return obj !== null && typeof obj === 'object' && !Array.isArray(obj)
}

/**
 * HTTP 请求工具类，提供 get/post/put/delete 四个方法
 *
 * 使用示例:
 *   import Http from '~/utils/request'
 *   const res = await Http.get('/v1/threads/view/248')  // res.data 即为后端返回数据
 *   const res = await Http.post('/v1/threads', { title: 'Hello' })
 */
export default new (class Http {
  get(url: string, params?: any): Promise<any> {
    if (params && isObject(params)) {
      // 过滤 null/undefined/空字符串参数，避免发送无效查询参数
      params = Object.fromEntries(
        Object.entries(params).filter(([_, v]) => v !== null && v !== undefined && v !== '')
      )
    }
    return fetch(url, { method: 'get', params })
  }

  post(url: string, body?: any): Promise<any> {
    return fetch(url, { method: 'post', body })
  }

  put(url: string, body?: any): Promise<any> {
    return fetch(url, { method: 'put', body })
  }

  delete(url: string, body?: any): Promise<any> {
    return fetch(url, { method: 'delete', body })
  }
})()
