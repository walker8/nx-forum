import type { Editor } from '@tiptap/core'
import { onBeforeUnmount, ref, watch, type Ref } from 'vue'

export interface OutlineItem {
  id: string
  level: number
  text: string
  pos: number
}

interface UseEditorOutlineOptions {
  onChange?: (items: OutlineItem[]) => void
}

type EditorListener = (e: { editor: Editor }) => void

export const useEditorOutline = (
  editor: Ref<Editor | null>,
  options: UseEditorOutlineOptions = {}
) => {
  const outline = ref<OutlineItem[]>([])
  const activeId = ref<string>('')

  const refreshOutline = (instance: Editor) => {
    const items: OutlineItem[] = []
    const pendingUpdates: Array<{ pos: number; attrs: Record<string, any> }> = []

    instance.state.doc.descendants((node, pos) => {
      if (node.type.name !== 'heading') {
        return
      }

      let headingId = node.attrs.id as string | null
      const level = node.attrs.level ?? 1
      if (!headingId) {
        headingId = `heading-${pos}`
        pendingUpdates.push({
          pos,
          attrs: {
            ...node.attrs,
            id: headingId
          }
        })
      }

      items.push({
        id: headingId,
        level,
        text: node.textContent.trim(),
        pos
      })
    })

    if (pendingUpdates.length) {
      const transaction = instance.state.tr
      pendingUpdates.forEach(({ pos, attrs }) => {
        transaction.setNodeMarkup(pos, undefined, attrs)
      })
      if (transaction.steps.length > 0) {
        instance.view.dispatch(transaction)
      }
    }

    outline.value = items
    options.onChange?.(items)
  }

  const updateActiveHeading = (instance: Editor) => {
    if (!outline.value.length) {
      activeId.value = ''
      return
    }

    const currentPos = instance.state.selection.from
    let current = outline.value[0].id
    for (const item of outline.value) {
      if (item.pos <= currentPos) {
        current = item.id
      } else {
        break
      }
    }
    activeId.value = current
  }

  const attachListeners = (instance: Editor) => {
    const onUpdate: EditorListener = () => refreshOutline(instance)
    const onSelection: EditorListener = () => updateActiveHeading(instance)

    instance.on('create', onUpdate)
    instance.on('update', onUpdate)
    instance.on('selectionUpdate', onSelection)

    refreshOutline(instance)
    updateActiveHeading(instance)

    return () => {
      instance.off('create', onUpdate)
      instance.off('update', onUpdate)
      instance.off('selectionUpdate', onSelection)
    }
  }

  let cleanup: (() => void) | null = null

  watch(
    editor,
    (instance) => {
      cleanup?.()
      cleanup = null
      if (instance) {
        cleanup = attachListeners(instance)
      }
    },
    { immediate: true }
  )

  onBeforeUnmount(() => {
    cleanup?.()
  })

  const scrollToHeading = (instance: Editor | null, id: string) => {
    if (!instance) return
    const target = outline.value.find((item) => item.id === id)
    if (!target) return

    instance.chain().focus().setTextSelection(target.pos).run()

    setTimeout(() => {
      const element = document.getElementById(id)
      if (element) {
        const container = document.querySelector('.editor-main')
        if (container) {
          const offset = 80 // 增加顶部留白，防止被工具栏遮挡
          const elementRect = element.getBoundingClientRect()
          const containerRect = container.getBoundingClientRect()
          const scrollTop = container.scrollTop + (elementRect.top - containerRect.top) - offset
          
          container.scrollTo({
            top: scrollTop,
            behavior: 'smooth'
          })
        } else {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      }
    }, 10)
  }

  return {
    outline,
    activeId,
    scrollToHeading: (id: string) => scrollToHeading(editor.value, id),
    refreshOutline: () => editor.value && refreshOutline(editor.value)
  }
}

