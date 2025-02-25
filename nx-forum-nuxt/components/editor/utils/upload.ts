import { Editor } from "@tiptap/core";
import Image from "../extensions/image";
import { uploadImage as uploadImageApi } from "@/apis/image";
import { ElMessage } from "element-plus";

export interface FileProps {
  file: File;
  editor: Editor;
}

export const handleFileEvent = ({ file, editor }: FileProps) => {
  if (!file) {
    return false;
  }

  if (file.type.startsWith("image/")) {
    uploadImage({ file, editor });
    return true;
  }

  // Video and Audio support can be added later
  return false;
};

export const uploadImage = async ({ file, editor }: FileProps) => {
  const formData = new FormData();
  formData.append("file", file);

  try {
    // Insert a placeholder or loading state if desired
    // For now, we upload then insert
    const res = await uploadImageApi(formData);
    const url = res.data;
    
    editor.chain().focus().setImage({ src: url, alt: file.name }).run();
  } catch (error) {
    console.error("Upload failed", error);
    ElMessage.error("图片上传失败");
  }
};

export function containsFileClipboardIdentifier(types: readonly string[]) {
  const fileTypes = ["files", "application/x-moz-file", "public.file-url"];
  // In some browsers, types might be a DOMStringList which behaves like an array
  // but let's be safe
  const typesArray = Array.from(types);
  return typesArray.some((type) => fileTypes.includes(type.toLowerCase()));
}
