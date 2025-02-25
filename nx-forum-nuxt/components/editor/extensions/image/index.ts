import { mergeAttributes } from "@tiptap/core";
import Image from "@tiptap/extension-image";
import { VueNodeViewRenderer } from "@tiptap/vue-3";
import ImageView from "./ImageView.vue";

export interface ImageOptions {
  inline: boolean;
  allowBase64: boolean;
  HTMLAttributes: Record<string, any>;
}

export interface UiImageOptions {
  uploadImage?: (file: File) => Promise<string>;
}

export default Image.extend<ImageOptions & UiImageOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      uploadImage: undefined,
      inline: false,
      allowBase64: false,
      HTMLAttributes: {},
    };
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: "100%",
        parseHTML: (element) => {
          const width =
            element.getAttribute("width") || element.style.width || null;
          return width;
        },
        renderHTML: (attributes) => {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: "auto",
        parseHTML: (element) => {
          const height =
            element.getAttribute("height") || element.style.height || null;
          return height;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      file: {
        default: null,
        renderHTML: () => ({}),
        parseHTML: () => null,
      },
      align: {
        default: 'left',
        renderHTML: (attributes) => {
          return {
            'data-align': attributes.align,
          };
        },
        parseHTML: (element) => element.getAttribute('data-align'),
      },
    };
  },

  addNodeView() {
    return VueNodeViewRenderer(ImageView);
  },
});
