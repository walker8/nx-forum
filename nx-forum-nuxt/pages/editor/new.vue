<template>
  <div class="new">
    <ClientOnly>
      <EditorTiptap
        v-if="mounted"
        v-model="threadCmd.content"
        v-model:title="threadCmd.subject"
        v-model:cover="threadCmd.cover"
      />
      <template #fallback>
        <div class="editor-loading">
          <el-skeleton :rows="10" animated />
        </div>
      </template>
    </ClientOnly>
  </div>
</template>
<script setup lang="ts">
definePageMeta({
  layout: 'editor'
})

// 懒加载编辑器组件
const EditorTiptap = defineAsyncComponent(() => import('~/components/editor/tiptap.vue'))
const mounted = ref(false)

onMounted(() => {
  mounted.value = true
})

const threadCmd = useThreadCmd()
threadCmd.value.threadId = 0
threadCmd.value.forumId = 0

useSeoMeta({
  title: '新建文章'
})
</script>
<style lang="scss" scoped>
.new {
  min-height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  background: #f5f7fb;
}

.editor-loading {
  width: 100%;
  padding: 20px;
}
</style>
