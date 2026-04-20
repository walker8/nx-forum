export default defineEventHandler(() => {
  const mem = process.memoryUsage()
  return {
    msg: "healthy",
    data: new Date(),
    memory: {
      rss: Math.round(mem.rss / 1024 / 1024),
      heapUsed: Math.round(mem.heapUsed / 1024 / 1024),
      heapTotal: Math.round(mem.heapTotal / 1024 / 1024),
      external: Math.round(mem.external / 1024 / 1024),
      uptime: Math.round(process.uptime()),
    }
  }
})
