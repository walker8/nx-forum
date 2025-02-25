<script lang="ts" setup>
import { NodeViewContent, NodeViewWrapper, nodeViewProps } from '@tiptap/vue-3'
import { useTimeout } from '@vueuse/core'
import { TextSelection } from '@tiptap/pm/state'
import CodeBlockSelect from './CodeBlockSelect.vue'

const props = defineProps(nodeViewProps)

const languages = computed(() => {
  return props.extension.options.languages || []
})

const selectedLanguage = computed({
  get: () => {
    return props.node.attrs.language || 'auto'
  },
  set: (language: string) => {
    props.updateAttributes({ language })
  },
})

const collapsed = computed<boolean>({
  get: () => {
    return props.node.attrs.collapsed || false
  },
  set: (collapsed: boolean) => {
    props.updateAttributes({ collapsed })
  },
})

const { ready, start } = useTimeout(2000, { controls: true, immediate: false })

const handleCopyCode = () => {
  if (!ready.value) return
  const code = props.node.textContent
  navigator.clipboard.writeText(code).then(() => {
    start()
  })
}

const handleDeleteCode = () => {
  const editor = props.editor
  
  // 保存当前滚动位置
  const scrollContainer = editor.view.dom.closest('.editor-content') as HTMLElement | null
  const scrollTop = scrollContainer 
    ? scrollContainer.scrollTop
    : window.pageYOffset || document.documentElement.scrollTop
  
  // 获取代码块节点的位置
  const getPos = props.getPos as (() => number) | undefined
  if (typeof getPos !== 'function') return
  
  const pos = getPos()
  if (pos === undefined) return
  
  // 计算删除后光标应该放置的位置（代码块前面的位置）
  const targetPos = Math.max(0, pos - 1)
  
  // 删除代码块
  editor
    .chain()
    .command(({ tr, dispatch }) => {
      if (dispatch) {
        // 删除代码块节点
        const nodeSize = props.node.nodeSize
        tr.delete(pos, pos + nodeSize)
        
        // 设置光标位置到代码块前面的位置
        const safePos = Math.max(0, Math.min(targetPos, tr.doc.content.size))
        try {
          const $pos = tr.doc.resolve(safePos)
          tr.setSelection(TextSelection.near($pos))
        } catch {
          // 如果位置无效，使用文档开始位置
          tr.setSelection(TextSelection.near(tr.doc.resolve(0)))
        }
      }
      return true
    })
    .focus()
    .run()
  
  // 恢复滚动位置
  nextTick(() => {
    if (scrollContainer) {
      scrollContainer.scrollTop = scrollTop
    } else {
      window.scrollTo(0, scrollTop)
    }
  })
}
</script>

<template>
  <node-view-wrapper class="code-block-view">
    <div class="code-block-header" contenteditable="false">
      <div class="left-controls">
        <button class="collapse-btn" @click="collapsed = !collapsed">
          <Icon :name="collapsed ? 'tabler:caret-right-filled' : 'tabler:caret-down-filled'" />
        </button>
        <CodeBlockSelect v-model="selectedLanguage" :options="languages" />
      </div>
      <div class="right-controls">
        <button class="copy-btn" @click="handleCopyCode" :title="ready ? '复制' : '已复制'">
          <Icon v-if="!ready" name="tabler:check" class="text-green-500" />
          <Icon v-else name="tabler:copy" />
        </button>
        <button class="delete-btn" @click="handleDeleteCode" title="删除代码块">
          <Icon name="tabler:trash" />
        </button>
      </div>
    </div>
    <pre v-show="!collapsed"><node-view-content as="code" class="hljs" /></pre>
  </node-view-wrapper>
</template>

<style scoped lang="scss">
.code-block-view {
  background-color: #f8f9fa;
  border-radius: 0.5rem;
  border: 1px solid #e2e8f0;
  margin: 1rem 0;
  overflow: hidden;

  .code-block-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.25rem 0.5rem;
    background-color: #f1f5f9;
    border-bottom: 1px solid #e2e8f0;
    user-select: none;

    .left-controls {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .right-controls {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .collapse-btn,
    .copy-btn,
    .delete-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 1.5rem;
      height: 1.5rem;
      border-radius: 0.25rem;
      color: #64748b;
      cursor: pointer;
      transition: background-color 0.2s;
      border: none;
      background: transparent;

      &:hover {
        background-color: #e2e8f0;
        color: #1e293b;
      }
    }

    .delete-btn {
      &:hover {
        background-color: #fee2e2;
        color: #ef4444;
      }
    }
  }

  pre {
    margin: 0;
    padding: 1rem;
    overflow-x: auto;
    font-family: 'JetBrains Mono', Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
    font-size: 0.875rem;
    line-height: 1.5;
    background-color: #ffffff;

    code {
      background: none;
      padding: 0;
      color: inherit;
    }
  }
}
</style>
