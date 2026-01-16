<script lang="ts" setup>
import { NodeViewWrapper, nodeViewProps } from '@tiptap/vue-3'
import { useTimeout } from '@vueuse/core'
import { TextSelection } from '@tiptap/pm/state'
import { nextTick, onMounted, ref, watch } from 'vue'

const props = defineProps(nodeViewProps)

const code = computed<string>({
  get: () => {
    return props.node.attrs.code || ''
  },
  set: (code: string) => {
    props.updateAttributes({ code })
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

const isPreviewMode = ref(false)
const renderedSvg = ref('')
const renderError = ref('')
const mermaidLoaded = ref(false)
const previewContainerRef = ref<HTMLElement | null>(null)

const { ready, start } = useTimeout(2000, { controls: true, immediate: false })

// Load mermaid dynamically
const loadMermaid = async () => {
  if (mermaidLoaded.value) return

  try {
    const mermaidModule = await import('mermaid')
    const mermaid = mermaidModule.default

    // Initialize mermaid with security settings
    mermaid.initialize({
      startOnLoad: false,
      securityLevel: 'loose',
      theme: 'default',
    })

    mermaidLoaded.value = true
  } catch (error) {
    console.error('Failed to load mermaid:', error)
    renderError.value = '无法加载 Mermaid 库'
  }
}

// Render mermaid diagram
const renderDiagram = async () => {
  if (!isPreviewMode.value) return

  renderError.value = ''
  renderedSvg.value = ''

  await loadMermaid()

  if (!mermaidLoaded.value) {
    renderError.value = 'Mermaid 库未加载'
    return
  }

  try {
    const mermaidModule = await import('mermaid')
    const mermaid = mermaidModule.default

    // Generate unique ID for this diagram
    const id = `mermaid-${Math.random().toString(36).substring(2, 9)}`

    // Render the diagram
    const { svg } = await mermaid.render(id, code.value)
    renderedSvg.value = svg
  } catch (error) {
    console.error('Mermaid render error:', error)
    renderError.value = '图表渲染失败，请检查语法是否正确'
  }
}

// Watch for mode changes
watch(isPreviewMode, async (newVal) => {
  if (newVal) {
    await nextTick()
    await renderDiagram()
  } else {
    renderedSvg.value = ''
    renderError.value = ''
  }
})

// Watch for code changes when in preview mode
watch(code, async () => {
  if (isPreviewMode.value) {
    await renderDiagram()
  }
})

const handleCopyCode = () => {
  if (!ready.value) return
  navigator.clipboard.writeText(code.value).then(() => {
    start()
  })
}

const handleDelete = () => {
  const editor = props.editor

  // Save current scroll position
  const scrollContainer = editor.view.dom.closest('.editor-content') as HTMLElement | null
  const scrollTop = scrollContainer
    ? scrollContainer.scrollTop
    : window.pageYOffset || document.documentElement.scrollTop

  // Get mermaid node position
  const getPos = props.getPos as (() => number) | undefined
  if (typeof getPos !== 'function') return

  const pos = getPos()
  if (pos === undefined) return

  // Calculate target cursor position (before the node)
  const targetPos = Math.max(0, pos - 1)

  // Delete the mermaid node
  editor
    .chain()
    .command(({ tr, dispatch }) => {
      if (dispatch) {
        // Delete the mermaid node
        const nodeSize = props.node.nodeSize
        tr.delete(pos, pos + nodeSize)

        // Set cursor position
        const safePos = Math.max(0, Math.min(targetPos, tr.doc.content.size))
        try {
          const $pos = tr.doc.resolve(safePos)
          tr.setSelection(TextSelection.near($pos))
        } catch {
          // If position is invalid, use document start
          tr.setSelection(TextSelection.near(tr.doc.resolve(0)))
        }
      }
      return true
    })
    .focus()
    .run()

  // Restore scroll position
  nextTick(() => {
    if (scrollContainer) {
      scrollContainer.scrollTop = scrollTop
    } else {
      window.scrollTo(0, scrollTop)
    }
  })
}

const toggleEditMode = () => {
  isPreviewMode.value = !isPreviewMode.value
}
</script>

<template>
  <node-view-wrapper class="mermaid-node-view">
    <div class="mermaid-container" data-type="mermaid">
      <div class="mermaid-header" contenteditable="false">
        <div class="left-controls">
          <button class="collapse-btn" @click="collapsed = !collapsed" title="折叠/展开">
            <Icon :name="collapsed ? 'tabler:caret-right-filled' : 'tabler:caret-down-filled'" />
          </button>
          <span class="mermaid-label">Mermaid</span>
        </div>
        <div class="right-controls">
          <button
            class="mode-btn"
            @click="toggleEditMode"
            :title="isPreviewMode ? '编辑模式' : '预览模式'"
          >
            <Icon :name="isPreviewMode ? 'tabler:pencil' : 'tabler:eye'" />
          </button>
          <button class="copy-btn" @click="handleCopyCode" :title="ready ? '复制代码' : '已复制'">
            <Icon v-if="!ready" name="tabler:check" class="text-green-500" />
            <Icon v-else name="tabler:copy" />
          </button>
          <button class="delete-btn" @click="handleDelete" title="删除图表">
            <Icon name="tabler:trash" />
          </button>
        </div>
      </div>
      <div v-show="!collapsed" class="mermaid-content">
        <!-- Edit Mode -->
        <textarea
          v-if="!isPreviewMode"
          v-model="code"
          class="mermaid-textarea"
          placeholder="输入 Mermaid 代码，例如：
graph TD
    A[开始] --> B{判断}
    B -->|是| C[执行]
    B -->|否| D[跳过]
    C --> E[结束]
    D --> E"
          rows="8"
        />
        <!-- Preview Mode -->
        <div v-else ref="previewContainerRef" class="mermaid-preview">
          <div v-if="renderError" class="mermaid-error">
            <Icon name="tabler:alert-circle" />
            <span>{{ renderError }}</span>
          </div>
          <div v-else-if="renderedSvg" v-html="renderedSvg" class="mermaid-diagram" />
          <div v-else class="mermaid-loading">
            <Icon name="tabler:loader-2" class="animate-spin" />
            <span>渲染中...</span>
          </div>
        </div>
      </div>
    </div>
  </node-view-wrapper>
</template>

<style scoped lang="scss">
.mermaid-node-view {
  margin: 1rem 0;

  .mermaid-container {
    background-color: #f8f9fa;
    border-radius: 0.5rem;
    border: 1px solid #e2e8f0;
    overflow: hidden;

    .mermaid-header {
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

        .mermaid-label {
          font-size: 0.875rem;
          font-weight: 500;
          color: #64748b;
        }
      }

      .right-controls {
        display: flex;
        align-items: center;
        gap: 0.25rem;
      }

      .collapse-btn,
      .mode-btn,
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

    .mermaid-content {
      background-color: #ffffff;

      .mermaid-textarea {
        width: 100%;
        padding: 1rem;
        border: none;
        resize: vertical;
        font-family: 'JetBrains Mono', Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
        font-size: 0.875rem;
        line-height: 1.5;
        background-color: #ffffff;
        color: #1e293b;
        outline: none;
        min-height: 150px;

        &::placeholder {
          color: #94a3b8;
        }
      }

      .mermaid-preview {
        padding: 1rem;
        min-height: 150px;
        display: flex;
        align-items: center;
        justify-content: center;

        .mermaid-diagram {
          text-align: center;
          width: 100%;
          overflow-x: auto;

          :deep(svg) {
            max-width: 100%;
            height: auto;
          }
        }

        .mermaid-error {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          padding: 0.75rem 1rem;
          background-color: #fef0f0;
          border: 1px solid #f56c6c;
          border-radius: 0.375rem;
          color: #f56c6c;
          font-size: 0.875rem;

          svg {
            width: 1rem;
            height: 1rem;
          }
        }

        .mermaid-loading {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          color: #94a3b8;
          font-size: 0.875rem;

          svg {
            width: 1rem;
            height: 1rem;
          }
        }
      }
    }
  }
}
</style>
