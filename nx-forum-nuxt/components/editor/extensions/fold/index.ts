import { Node, mergeAttributes } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import FoldView from './FoldView.vue'

export interface FoldOptions {
  HTMLAttributes: Record<string, any>
}

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    fold: {
      setFold: (options?: { collapsed?: boolean; title?: string }) => ReturnType
      toggleFold: (options?: { collapsed?: boolean; title?: string }) => ReturnType
      unsetFold: () => ReturnType
    }
  }
}

export default Node.create<FoldOptions>({
  name: 'fold',

  group: 'block',

  content: 'block+',

  draggable: true,

  addOptions() {
    return {
      HTMLAttributes: {},
    }
  },

  addAttributes() {
    return {
      collapsed: {
        default: false,
        parseHTML: element => element.getAttribute('data-collapsed') === 'true',
        renderHTML: attributes => {
          return { 'data-collapsed': attributes.collapsed }
        },
      },
      title: {
        default: '点击展开',
        parseHTML: element => element.getAttribute('data-title'),
        renderHTML: attributes => {
          return { 'data-title': attributes.title }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="fold"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, { class: 'fold-block', 'data-type': 'fold' }), 0]
  },

  addNodeView() {
    return VueNodeViewRenderer(FoldView)
  },

  addCommands() {
    return {
      setFold:
        (options = {}) =>
        ({ commands }) => {
          return commands.wrapIn(this.name, {
            collapsed: options.collapsed ?? false,
            title: options.title ?? '点击展开',
          })
        },
      toggleFold:
        (options = {}) =>
        ({ commands }) => {
          return commands.toggleWrap(this.name, {
            collapsed: options.collapsed ?? false,
            title: options.title ?? '点击展开',
          })
        },
      unsetFold:
        () =>
        ({ commands }) => {
          return commands.lift(this.name)
        },
    }
  },
})
