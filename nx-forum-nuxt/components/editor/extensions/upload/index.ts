import { Extension } from "@tiptap/core";
import { Plugin, PluginKey } from "@tiptap/pm/state";
import { containsFileClipboardIdentifier, handleFileEvent } from "../../utils/upload";

export const Upload = Extension.create({
  name: "upload",

  addProseMirrorPlugins() {
    const { editor } = this;

    return [
      new Plugin({
        key: new PluginKey("upload"),
        props: {
          handlePaste: (view, event) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.clipboardData) {
              return false;
            }

            const types = event.clipboardData.types;
            // Only process when a single file is pasted.
            if (types.length > 1 && !containsFileClipboardIdentifier(types)) {
               // If it has other types but NOT file types, ignore. 
               // But if it has file types mixed with others, we might want to handle it?
               // Halo logic: if (types.length > 1) return false; 
               // But clipboard often has 'text/plain', 'text/html', 'Files'.
               // Let's stick to checking for files.
            }

            if (!containsFileClipboardIdentifier(types)) {
              return false;
            }

            const files = Array.from(event.clipboardData.files);

            if (files.length) {
              event.preventDefault();
              files.forEach((file) => {
                handleFileEvent({ editor, file });
              });
              return true;
            }

            return false;
          },
          handleDrop: (view, event) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false;
            }

            if (!event.dataTransfer) {
              return false;
            }

            const hasFiles = event.dataTransfer.files.length > 0;
            if (!hasFiles) {
              return false;
            }

            event.preventDefault();

            const files = Array.from(event.dataTransfer.files);
            if (files.length) {
              event.preventDefault();
              files.forEach((file) => {
                handleFileEvent({ editor, file });
              });
              return true;
            }

            return false;
          },
        },
      }),
    ];
  },
});

export default Upload;
