<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { createReport } from '~/apis/report'
import type { CreateReportCommand } from '~/types/global'

const props = defineProps<{
  visible: boolean
  targetId: number
  targetType: 'THREAD' | 'COMMENT' | 'REPLY'
  forumId: number
}>()

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const reportForm = reactive<{
  reportType: CreateReportCommand['reportType'] | ''
  reportReason: string
}>({
  reportType: '',
  reportReason: ''
})

// 确保所有值符合CreateReportCommand['reportType']类型
const reportTypes: { value: CreateReportCommand['reportType']; label: string }[] = [
  { value: 'SPAM', label: '垃圾广告' },
  { value: 'PORN', label: '色情低俗' },
  { value: 'HATE_SPEECH', label: '仇恨言论' },
  { value: 'COPYRIGHT', label: '涉嫌侵权' },
  { value: 'DISORDERLY', label: '扰乱社区秩序' },
  { value: 'VIOLATION', label: '违法违规' },
  { value: 'OTHER', label: '其他原因' }
]

const loading = ref(false)
const isMobile = ref(false)
const isClient = import.meta.client

// 获取举报标题
function getReportTitle() {
  const titleMap = {
    THREAD: '举报文章',
    COMMENT: '举报评论',
    REPLY: '举报回复'
  }

  return titleMap[props.targetType] || '举报'
}

// 检测是否为移动设备
onMounted(() => {
  if (isClient) {
    checkScreenSize()
    window.addEventListener('resize', checkScreenSize)
  }
})

onUnmounted(() => {
  if (isClient) {
    window.removeEventListener('resize', checkScreenSize)
  }
})

function checkScreenSize() {
  if (isClient) {
    isMobile.value = window.innerWidth < 768
  }
}

async function submitReport() {
  if (!reportForm.reportType) {
    ElMessage.error('请选择举报类型')
    return
  }
  if (reportForm.reportType === 'OTHER' && !reportForm.reportReason) {
    ElMessage.error('请输入举报原因')
    return
  }
  loading.value = true
  createReport({
    targetId: props.targetId,
    targetType: props.targetType,
    forumId: props.forumId,
    reportType: reportForm.reportType,
    reportReason: reportForm.reportReason
  })
    .then(() => {
      ElMessage.success('举报成功，感谢您的反馈')
      dialogVisible.value = false
    })
    .catch((err) => {
      ElMessage.error(err || '举报失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
    })
}

watch(dialogVisible, (val) => {
  if (!val) {
    reportForm.reportType = ''
    reportForm.reportReason = ''
  }
})
</script>

<template>
  <client-only>
    <el-dialog
      v-model="dialogVisible"
      :title="getReportTitle()"
      :width="isMobile ? '100%' : '500px'"
      :fullscreen="isMobile"
      :class="{ 'mobile-dialog': isMobile }"
      :top="isMobile ? '0' : '15vh'"
      :destroy-on-close="true"
      append-to-body
    >
      <div class="report-container">
        <div class="report-types-grid">
          <div
            v-for="item in reportTypes"
            :key="item.value"
            class="report-type-item"
            :class="{ active: reportForm.reportType === item.value }"
            @click="reportForm.reportType = item.value"
          >
            {{ item.label }}
          </div>
        </div>

        <div v-if="reportForm.reportType === 'OTHER'" class="mt-4">
          <el-input
            v-model="reportForm.reportReason"
            type="textarea"
            :rows="3"
            placeholder="请输入举报原因"
          />
        </div>

        <div class="report-footer">
          <el-button
            type="primary"
            :loading="loading"
            @click="submitReport"
            class="submit-btn w-full"
          >
            提交举报
          </el-button>
        </div>
      </div>
    </el-dialog>
  </client-only>
</template>

<style scoped>
.mobile-dialog :deep(.el-dialog__header) {
  display: none;
}

.mobile-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.report-container {
  padding: 0;
}

.report-title {
  text-align: center;
  margin-bottom: 20px;
}

.report-types-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.report-type-item {
  border: 1px solid #ebeef5;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 12px 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.report-type-item.active {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
}

.report-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 767px) {
  .report-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 10px 20px;
    background-color: #fff;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    margin-top: 0;
  }

  .submit-btn {
    max-width: none;
    height: 44px;
    border-radius: 22px;
    font-size: 16px;
  }

  .report-types-grid {
    margin-bottom: 60px;
  }
}
</style>
