module.exports = {
  apps: [
    {
      name: 'nx-forum-nuxt', // 设置启动项目名称
      exec_mode: 'cluster',
      instances: 2,
      // 注意这里的相对路径。要访问到index.mjs就行了，如果你是整个.output一起放在服务器的话就和官方一样路写成./.output/server/index.mjs就好了
      script: './server/index.mjs',
      // 内存超过阈值自动重启，防止 OOM 崩溃
      max_memory_restart: '900M',
      // 限制 V8 堆大小，使其更早触发 GC，避免接近默认上限（~1.4GB）时性能骤降
      node_args: '--max-old-space-size=1024'
    }
  ]
}
