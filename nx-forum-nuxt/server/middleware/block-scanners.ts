import { createError, defineEventHandler } from 'h3'

// 已知扫描攻击路径前缀，用 startsWith 快速匹配避免正则开销
const blockedPrefixes = [
  '/actuator', '/v3/api-docs', '/api/debug', '/swagger',
  '/.env', '/.git', '/xmlrpc', '/adminer', '/phpmyadmin',
  '/.well-known/security.txt', '/.well-known/assetlink.json',
]

// 安全路径前缀，命中时跳过扫描检测（覆盖绝大多数正常流量）
const safePrefixes = ['/nx-forum/', '/_nuxt/', '/api/health']

export default defineEventHandler((event) => {
  const pathname = event.path

  // 跳过已知安全路径，避免对正常请求做无谓检测
  for (const prefix of safePrefixes) {
    if (pathname.startsWith(prefix)) return
  }

  // 前缀匹配
  for (const prefix of blockedPrefixes) {
    if (pathname.startsWith(prefix)) {
      throw createError({ statusCode: 404, statusMessage: 'Not Found' })
    }
  }

  // 后缀匹配：.php 结尾和特定配置文件
  if (pathname.endsWith('.php') || /^\/config\.(json|yml|yaml)$/.test(pathname)) {
    throw createError({ statusCode: 404, statusMessage: 'Not Found' })
  }
})
