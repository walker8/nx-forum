import {mergeAttributes, Node} from '@tiptap/core'
import {VueNodeViewRenderer} from '@tiptap/vue-3'
import MermaidView from './MermaidView.vue'

export interface MermaidOptions {
    HTMLAttributes: Record<string, any>
}

declare module '@tiptap/core' {
    interface Commands<ReturnType> {
        mermaid: {
            insertMermaid: (options?: { code: string }) => ReturnType
        }
    }
}

export default Node.create<MermaidOptions>({
    name: 'mermaid',

    group: 'block',

    inline: false,

    atom: true,

    draggable: true,

    addOptions() {
        return {
            HTMLAttributes: {},
        }
    },

    addAttributes() {
        return {
            code: {
                default: '',
                parseHTML: element => element.getAttribute('data-code'),
                renderHTML: attributes => {
                    return {'data-code': attributes.code}
                },
            },
            collapsed: {
                default: false,
                parseHTML: element => element.getAttribute('data-collapsed') === 'true',
                renderHTML: attributes => {
                    return {'data-collapsed': attributes.collapsed ? 'true' : 'false'}
                },
            },
        }
    },

    parseHTML() {
        return [
            {
                tag: 'div[data-type="mermaid"]',
            },
        ]
    },

    renderHTML({HTMLAttributes}) {
        return ['div', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {'data-type': 'mermaid'})]
    },

    addNodeView() {
        return VueNodeViewRenderer(MermaidView)
    },

    addCommands() {
        return {
            insertMermaid:
                (options = {code: ''}) =>
                    ({commands}) => {
                        return commands.insertContent({
                            type: this.name,
                            attrs: options,
                        })
                    },
        }
    },
})
