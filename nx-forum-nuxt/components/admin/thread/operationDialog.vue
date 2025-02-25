<template>
  <div class="operation-dialog">
    <el-dialog v-model="dialogVisible" :title="title" style="max-width: 350px">
      <el-form>
        <el-form-item>
          <el-text>当前选中{{ threadIds?.length }}篇主题</el-text>
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="selectedOption">
            <el-radio
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { OperationConstant } from '~/constant'

const dialogVisible = ref(false)
const selectedOption = ref('')
const options = ref()
const threadIds = ref<number[]>([])
const title = ref('')
const emits = defineEmits(['confirm'])
const confirm = () => {
  emits('confirm', selectedOption.value, threadIds.value)
  dialogVisible.value = false
}
const showDialog = (operation: string, ids: number[]) => {
  dialogVisible.value = true
  threadIds.value = ids
  selectedOption.value = operation
  if (operation === OperationConstant.TOP) {
    options.value = [
      { value: OperationConstant.TOP, label: '版块置顶' },
      { value: OperationConstant.GLOBAL_TOP, label: '全局置顶' },
      { value: OperationConstant.CANCEL_TOP, label: '取消置顶' }
    ]
    title.value = '置顶操作'
  } else if (operation === OperationConstant.DIGEST) {
    options.value = [
      { value: OperationConstant.DIGEST, label: '加精' },
      { value: OperationConstant.CANCEL_DIGEST, label: '取消加精' }
    ]
    title.value = '精华操作'
  } else if (operation === OperationConstant.CLOSE) {
    options.value = [
      { value: OperationConstant.CLOSE, label: '关闭' },
      { value: OperationConstant.OPEN, label: '打开' }
    ]
    title.value = '关闭操作'
  } else if (operation === OperationConstant.RECOMMEND) {
    options.value = [
      { value: OperationConstant.RECOMMEND, label: '推荐' },
      { value: OperationConstant.CANCEL_RECOMMEND, label: '取消推荐' }
    ]
    title.value = '推荐操作'
  }
}
defineExpose({ showDialog })
</script>
