<template>
  <el-select v-model="modelValue" filterable placeholder="选择语言" class="code-block-select" size="small"
    @change="emit('select')" @visible-change="handleVisibleChange">
    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
  </el-select>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, defineModel } from 'vue'

defineProps<{
  options: { label: string; value: string }[]
}>()

const modelValue = defineModel<string>()
const emit = defineEmits(['select'])

const handleVisibleChange = (visible: boolean) => {
  if (!visible) {
    // Optional: handle blur or other logic if needed
  }
}
</script>

<style scoped>
.code-block-select {
  width: 120px;
}

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  background-color: transparent;
  padding: 0;
}

:deep(.el-input__inner) {
  font-size: 12px;
  color: #666;
}
</style>
