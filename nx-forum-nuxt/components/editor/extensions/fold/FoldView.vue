<script lang="ts" setup>
import { NodeViewContent, NodeViewWrapper, nodeViewProps } from '@tiptap/vue-3'
import { TextSelection } from '@tiptap/pm/state'

const props = defineProps(nodeViewProps)

const collapsed = computed<boolean>({
  get: () => {
    return props.node.attrs.collapsed || false
  },
  set: (collapsed: boolean) => {
    props.updateAttributes({ collapsed })
  },
})

const title = computed<string>({
  get: () => {
    return props.node.attrs.title || '点击展开'
  },
  set: (title: string) => {
    props.updateAttributes({ title })
  },
})

const handleDelete = () => {
  const editor = props.editor

  // 保存当前滚动位置
  const scrollContainer = editor.view.dom.closest('.editor-content') as HTMLElement | null
  const scrollTop = scrollContainer
    ? scrollContainer.scrollTop
    : window.pageYOffset || document.documentElement.scrollTop

  // 获取折叠面板节点的位置
  const getPos = props.getPos as (() => number) | undefined
  if (typeof getPos !== 'function') return

  const pos = getPos()
  if (pos === undefined) return

  // 计算删除后光标应该放置的位置
  const targetPos = Math.max(0, pos - 1)

  // 删除折叠面板
  editor
    .chain()
    .command(({ tr, dispatch }) => {
      if (dispatch) {
        // 删除节点
        const nodeSize = props.node.nodeSize
        tr.delete(pos, pos + nodeSize)

        // 设置光标位置到前面
        const safePos = Math.max(0, Math.min(targetPos, tr.doc.content.size))
        try {
          const $pos = tr.doc.resolve(safePos)
          tr.setSelection(TextSelection.near($pos))
        } catch {
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

const isEditing = ref(false)
const editTitleRef = ref<HTMLInputElement | null>(null)

const startEditing = () => {
  isEditing.value = true
  nextTick(() => {
    editTitleRef.value?.focus()
    editTitleRef.value?.select()
  })
}

const finishEditing = () => {
  isEditing.value = false
}

const handleTitleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    finishEditing()
  }
  if (e.key === 'Escape') {
    isEditing.value = false
  }
}
</script>

<template>
  <node-view-wrapper class="fold-view">
    <div class="fold-header" contenteditable="false">
      <div class="left-controls">
        <button class="fold-btn" @click="collapsed = !collapsed">
          <Icon :name="collapsed ? 'tabler:caret-right-filled' : 'tabler:caret-down-filled'" />
        </button>
        <div class="title-wrapper" @dblclick="startEditing">
          <input
            v-if="isEditing"
            ref="editTitleRef"
            v-model="title"
            class="title-input"
            @blur="finishEditing"
            @keydown="handleTitleKeydown"
          />
          <span v-else class="title-text">{{ title }}</span>
        </div>
      </div>
      <div class="right-controls">
        <button class="delete-btn" @click="handleDelete" title="删除折叠面板">
          <Icon name="tabler:trash" />
        </button>
      </div>
    </div>
    <div v-show="!collapsed" class="fold-content">
      <node-view-content class="content" />
    </div>
  </node-view-wrapper>
</template>

<style scoped lang="scss">
.fold-view {
  background-color: #f8f9fa;
  border-radius: 0.5rem;
  border: 1px solid #e2e8f0;
  margin: 1rem 0;
  overflow: hidden;

  .fold-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.5rem 0.75rem;
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

    .fold-btn,
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

    .title-wrapper {
      display: flex;
      align-items: center;
      cursor: text;

      .title-text {
        font-size: 0.875rem;
        font-weight: 500;
        color: #334155;
      }

      .title-input {
        font-size: 0.875rem;
        font-weight: 500;
        color: #334155;
        background: white;
        border: 1px solid #3b82f6;
        border-radius: 0.25rem;
        padding: 0.125rem 0.375rem;
        outline: none;
        min-width: 100px;
      }
    }
  }

  .fold-content {
    padding: 0.75rem;
    background-color: #ffffff;

    .content {
      :deep(> *) {
        margin: 0;
      }

      :deep(p) {
        margin: 0.5em 0;

        &:first-child {
          margin-top: 0;
        }

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>
