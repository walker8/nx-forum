<template>
  <div class="mt-2.5" v-if="props.hasNext">
    <el-button style="width: 100%" type="info" plain @click="loadMore" :disabled="disableLoadMore">
      <span v-if="props.disableLoadMore"> 加载中... </span>
      <span v-else>
        <slot />
      </span>
    </el-button>
  </div>
</template>
<script setup lang="ts">
const props = defineProps({
  hasNext: {
    type: Boolean,
    defalut: false
  },
  disableLoadMore: {
    type: Boolean,
    default: false
  }
})
const emits = defineEmits(['loadMore'])
const loadMore = () => {
  emits('loadMore')
}
</script>
