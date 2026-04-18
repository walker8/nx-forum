// ECharts 已改为按需加载，由 composables/useEChartsInit.ts 在实际使用的页面中初始化
// 此插件保留为空以避免全局加载 ECharts 模块（节省约 460KB+ 首屏体积）
export default defineNuxtPlugin(() => {})
