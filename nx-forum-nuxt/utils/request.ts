import type { _AsyncData } from '#app/composables/asyncData'
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

const fetch = async (url: string, options?: any): Promise<any> => {
  const reqUrl = baseUrl + url
  let headers = {}
  if (import.meta.server) {
    headers = useRequestHeaders()
  }
  return new Promise((resolve, reject) => {
    const nuxtApp = useNuxtApp()
    const hydrating = import.meta.client && !nuxtApp.isHydrating
    if (hydrating) {
      // 客户端访问
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
            // 401未授权
            reject('当前用户无操作权限')
          } else {
            // 后端访问不了 500状态码
            reject('服务器内部错误')
          }
        })
    } else {
      // 服务器访问
      useFetch(reqUrl, {
        ...options,
        headers
      })
        .then(({ data, error }: _AsyncData<any, any>) => {
          if (error.value) {
            console.log('Request Error', error.value)
            if (error.value.statusCode === 401) {
              // 401未授权
              reject('当前用户无操作权限')
            } else {
              // 后端访问不了 500状态码
              reject('服务器内部错误')
            }
            return
          }
          const res = data.value
          if (!res) {
            // 这里处理错误回调
            // reject(value)
            // $router.replace('/reject/' + value.status)
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

export default new (class Http {
  get(url: string, params?: any): Promise<any> {
    if (params && isObject(params)) {
      // 过滤 null/undefined/空字符串参数
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
