<template>
  <node-view-wrapper class="alert-block" :class="`alert-${node.attrs.type}`">
    <div class="alert-icon" contenteditable="false">
      <Icon :name="iconName" />
    </div>
    <node-view-content class="alert-content" />
  </node-view-wrapper>
</template>

<script setup lang="ts">
import { NodeViewWrapper, NodeViewContent, nodeViewProps } from '@tiptap/vue-3'
import { computed } from 'vue'

const props = defineProps(nodeViewProps)

const iconName = computed(() => {
  switch (props.node.attrs.type) {
    case 'success':
      return 'tabler:circle-check-filled'
    case 'warning':
      return 'tabler:alert-circle-filled'
    case 'error':
      return 'tabler:circle-x-filled'
    case 'info':
    default:
      return 'tabler:info-circle-filled'
  }
})
</script>

<style scoped>
.alert-block {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  margin: 1rem 0;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.alert-icon {
  flex-shrink: 0;
  font-size: 16px;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  margin-top: 2px;
  /* 与第一行文字垂直居中 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.alert-content {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  line-height: 1.5;
}

/* Ensure paragraphs inside don't have extra margins */
:deep(.alert-content p) {
  margin: 0.5em 0;
}

:deep(.alert-content p:first-child) {
  margin-top: 0;
}

:deep(.alert-content p:last-child) {
  margin-bottom: 0;
}

/* Element Plus Colors */
/* Info */
.alert-info {
  background-color: #f4f4f5;
  color: #909399;
}

.alert-info .alert-icon {
  color: #909399;
}

/* Success */
.alert-success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.alert-success .alert-icon {
  color: #67c23a;
}

/* Warning */
.alert-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.alert-warning .alert-icon {
  color: #e6a23c;
}

/* Error */
.alert-error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.alert-error .alert-icon {
  color: #f56c6c;
}

/* Dark mode support - mimicking Element Plus dark theme */
:global(.dark) .alert-info {
  background-color: #202121;
  color: #a6a9ad;
}

:global(.dark) .alert-info .alert-icon {
  color: #a6a9ad;
}

:global(.dark) .alert-success {
  background-color: #1c2518;
  color: #67c23a;
}

:global(.dark) .alert-success .alert-icon {
  color: #67c23a;
}

:global(.dark) .alert-warning {
  background-color: #292218;
  color: #e6a23c;
}

:global(.dark) .alert-warning .alert-icon {
  color: #e6a23c;
}

:global(.dark) .alert-error {
  background-color: #2b1d1d;
  color: #f56c6c;
}

:global(.dark) .alert-error .alert-icon {
  color: #f56c6c;
}
</style>
