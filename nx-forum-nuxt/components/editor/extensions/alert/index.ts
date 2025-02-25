import { Node, mergeAttributes } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import AlertView from './AlertView.vue'

export interface AlertOptions {
  HTMLAttributes: Record<string, any>
}

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    alert: {
      setAlert: (options?: { type: 'info' | 'success' | 'warning' | 'error' }) => ReturnType
      toggleAlert: (options?: { type: 'info' | 'success' | 'warning' | 'error' }) => ReturnType
      unsetAlert: () => ReturnType
    }
  }
}

export default Node.create<AlertOptions>({
  name: 'alert',

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
      type: {
        default: 'info',
        parseHTML: element => element.getAttribute('data-type'),
        renderHTML: attributes => {
          return { 'data-type': attributes.type }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type]',
        getAttrs: element => {
          const type = (element as HTMLElement).getAttribute('data-type')
          if (['info', 'success', 'warning', 'error'].includes(type || '')) {
            return { type }
          }
          return false
        },
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, { class: 'alert-block' }), 0]
  },

  addNodeView() {
    return VueNodeViewRenderer(AlertView)
  },

  addCommands() {
    return {
      setAlert:
        (options = { type: 'info' }) =>
        ({ commands }) => {
          return commands.wrapIn(this.name, options)
        },
      toggleAlert:
        (options = { type: 'info' }) =>
        ({ commands }) => {
          return commands.toggleWrap(this.name, options)
        },
      unsetAlert:
        () =>
        ({ commands }) => {
          return commands.lift(this.name)
        },
    }
  },
})
