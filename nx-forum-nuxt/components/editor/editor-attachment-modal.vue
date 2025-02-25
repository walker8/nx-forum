<template>
  <el-dialog
    v-model="visible"
    width="420px"
    class="editor-attachment-dialog"
    :close-on-click-modal="false"
    :show-close="false"
  >
    <template #header>
      <div class="dialog-title">
        <Icon name="tabler:folder-plus" />
        <span>插入附件</span>
        <el-button text class="header-close" @click="visible = false">
          <Icon name="tabler:x" />
        </el-button>
      </div>
    </template>

    <div class="dialog-body">
      <p class="tip">支持 JPG/PNG/GIF/Markdown 等常见格式，单文件不超过 10MB</p>
      <el-button type="primary" :loading="uploading" @click="$emit('select')">
        {{ uploading ? '上传中...' : '选择文件' }}
      </el-button>
      <el-progress
        v-if="uploading"
        :percentage="Math.min(progress, 100)"
        :stroke-width="6"
        class="mt-4"
        striped
        striped-flow
      />
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        class="mt-4"
        show-icon
        closable="false"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  uploading: boolean
  progress: number
  error?: string | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})
</script>

<style scoped lang="scss">
.dialog-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
  padding-right: 8px;

  span {
    flex: 1;
    margin-left: 8px;
  }
}

.header-close {
  color: #94a3b8;
  padding: 4px;
}

.dialog-body {
  display: flex;
  flex-direction: column;
}

.tip {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 16px;
}

.mt-4 {
  margin-top: 16px;
}
</style>

