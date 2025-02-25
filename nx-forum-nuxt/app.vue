<template>
  <el-config-provider :locale="zhCn">
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
    <common-report-dialog 
      v-model:visible="reportDialogVisible" 
      :target-id="reportData.targetId"
      :target-type="reportData.targetType"
      :forum-id="reportData.forumId"
    />
  </el-config-provider>
</template>
<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './assets/styles/index.scss'
import { ID_INJECTION_KEY } from 'element-plus'
provide(ID_INJECTION_KEY, {
  prefix: 100,
  current: 0
})

// 全局使用举报状态
const { reportDialogVisible, reportData } = useReport()
</script>
