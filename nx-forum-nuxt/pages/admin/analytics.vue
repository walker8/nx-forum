<template>
  <div class="nx-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>统计设置</span>
          <el-alert
            title="安全提示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 10px"
          >
            <template #default>
              <div>
                <p>请仅使用可信的统计服务代码（如百度统计、Google Analytics 等）。</p>
                <p>恶意代码可能导致安全风险，请谨慎配置。</p>
              </div>
            </template>
          </el-alert>
        </div>
      </template>
      <el-form label-width="auto" ref="formRef">
        <el-form-item label="统计代码">
          <el-input
            v-model="analyticsCode"
            type="textarea"
            placeholder="请输入统计代码，例如百度统计代码：&#10;var _hmt = _hmt || [];&#10;(function() {&#10;  var hm = document.createElement(&quot;script&quot;);&#10;  hm.src = &quot;https://hm.baidu.com/hm.js?your_id&quot;;&#10;  var s = document.getElementsByTagName(&quot;script&quot;)[0];&#10;  s.parentNode.insertBefore(hm, s);&#10;})();"
            :autosize="{ minRows: 10, maxRows: 20 }"
            style="max-width: 800px"
          />
          <div class="form-tip">
            <p>• 支持百度统计、Google Analytics 等统计服务</p>
            <p>• 可以直接粘贴完整的统计代码（包含 &lt;script&gt; 标签或不包含均可）</p>
            <p>• 禁用后代码仍会保留，启用时即可生效</p>
          </div>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="enabled"
            @change="handleEnabledChange"
          />
          <span class="ml-2 text-gray-500">
            {{ enabled ? '统计代码已配置并启用' : '统计代码未配置或已禁用' }}
          </span>
        </el-form-item>
        <div class="el-form-item">
          <el-button type="primary" @click="onSubmit" :loading="loading">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance } from 'element-plus'
import {
  getAnalyticsConfigByAdmin,
  updateAnalyticsConfigByAdmin,
  type AnalyticsConfigDTO
} from '~/apis/config'

definePageMeta({
  layout: 'admin'
})

const formRef = ref<FormInstance>()
const loading = ref(false)
const analyticsCode = ref('')
const enabled = ref(true) // 默认启用

// 保存原始配置用于重置
const originalConfig = ref<AnalyticsConfigDTO>({
  analyticsCode: '',
  enabled: true // 默认启用
})

// 加载配置
const loadConfig = () => {
  loading.value = true
  getAnalyticsConfigByAdmin()
    .then((res) => {
      const config: AnalyticsConfigDTO = res.data
      analyticsCode.value = config.analyticsCode || ''
      // enabled 为 null 或 undefined 时默认为 true
      enabled.value = config.enabled !== undefined && config.enabled !== null ? config.enabled : true
      originalConfig.value = { ...config }
    })
    .catch((error) => {
      ElMessage.error(error || '获取统计配置失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 处理启用状态变化
const handleEnabledChange = (value: boolean) => {
  // 禁用时不删除代码，只是不生效
  // 代码保留，方便用户重新启用
}

// 提交表单
const onSubmit = () => {
  loading.value = true
  const config: AnalyticsConfigDTO = {
    analyticsCode: analyticsCode.value,
    enabled: enabled.value
  }
  
  updateAnalyticsConfigByAdmin(config)
    .then((res) => {
      originalConfig.value = { ...config }
      
      // 清除前端缓存，使新配置立即生效
      const { clearCache } = useAnalytics()
      clearCache()
      
      // 提示用户刷新页面使统计代码生效
      ElMessage.info('配置已保存，刷新页面后统计代码将生效')
    })
    .catch((error) => {
      ElMessage.error(error || '保存失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 页面加载时获取配置
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.form-tip p {
  margin: 4px 0;
}

.ml-2 {
  margin-left: 8px;
}

.text-gray-500 {
  color: #909399;
}
</style>

