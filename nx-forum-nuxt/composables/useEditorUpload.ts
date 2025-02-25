import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import { uploadImage } from '@/apis/image'

export interface UploadResult {
  url: string
  fileName: string
  mime?: string
  size: number
}

export interface UseEditorUploadOptions {
  uploader?: (formData: FormData) => Promise<{ data: string }>
  maxSizeMB?: number
  onError?: (message: string) => void
}

interface SelectOptions {
  accept?: string
  maxSizeMB?: number
  onSuccess?: (result: UploadResult) => void
}

export const useEditorUpload = (options: UseEditorUploadOptions = {}) => {
  const uploading = ref(false)
  const progress = ref(0)
  const errorMessage = ref<string | null>(null)

  const performUpload = async (file: File, sizeLimit?: number) => {
    const limit = sizeLimit ?? options.maxSizeMB
    if (limit && file.size / 1024 / 1024 > limit) {
      const message = `上传失败，文件大小请控制在 ${limit}MB 以内`
      errorMessage.value = message
      ElMessage.error(message)
      options.onError?.(message)
      throw new Error(message)
    }

    uploading.value = true
    progress.value = 10
    try {
      const formData = new FormData()
      formData.append('file', file)
      const uploader = options.uploader ?? uploadImage
      const res = await uploader(formData)
      progress.value = 100
      const result: UploadResult = {
        url: res.data,
        fileName: file.name,
        mime: file.type,
        size: file.size
      }
      errorMessage.value = null
      return result
    } catch (error: any) {
      const message = error?.message || '上传失败，请稍后重试'
      errorMessage.value = message
      ElMessage.error(message)
      options.onError?.(message)
      throw error
    } finally {
      setTimeout(() => {
        uploading.value = false
        progress.value = 0
      }, 200)
    }
  }

  const openFileDialog = (dialogOptions: SelectOptions = {}) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = dialogOptions.accept ?? '*/*'
    input.onchange = async (event: Event) => {
      const target = event.target as HTMLInputElement
      const file = target.files?.[0]
      if (!file) return
      try {
        const result = await performUpload(file, dialogOptions.maxSizeMB)
        dialogOptions.onSuccess?.(result)
      } catch {
        // 错误已在 performUpload 中处理
      } finally {
        target.value = ''
      }
    }
    input.click()
  }

  return {
    uploading,
    progress,
    errorMessage,
    uploadFile: performUpload,
    openFileDialog
  }
}

