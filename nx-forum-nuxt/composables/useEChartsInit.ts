import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent, LegendComponent } from 'echarts/components'

let initialized = false

function initECharts() {
  if (initialized) return
  use([CanvasRenderer, LineChart, BarChart, PieChart, GridComponent, TooltipComponent, TitleComponent, LegendComponent])
  initialized = true
}

export function useEChartsInit() {
  initECharts()
  const nuxtApp = useNuxtApp()
  if (!nuxtApp.vueApp.component('v-chart')) {
    nuxtApp.vueApp.component('v-chart', ECharts)
  }
}
