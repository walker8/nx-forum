<template>
  <el-image-viewer
    @close="closeImgViewer"
    :url-list="imageList"
    :initial-index="currentImageIndex"
    v-if="isShow"
  />
</template>
<script setup lang="ts">
import { showImagePreview } from 'vant'
// 提取图片 URL 的正则表达式
const imgRegex = /<img.*?src="(.*?)".*?originalsrc="(.*?)".*?>/g
// 从 HTML 中提取图片 URL
function extractImageUrls(htmlContent: string) {
  let imageUrls = []
  let originalImageUrls = []
  let match
  while ((match = imgRegex.exec(htmlContent)) !== null) {
    imageUrls.push(match[1])
    originalImageUrls.push(match[2])
  }
  return { imageUrls, originalImageUrls }
}
const imageList = ref<Array<string>>([])
const currentImageIndex = ref(0)
const isShow = ref(false)
const closeImgViewer = () => {
  isShow.value = false
}

const showImage = (element: any, content: string) => {
  if (element.target.nodeName == 'IMG') {
    let { imageUrls, originalImageUrls } = extractImageUrls(content)
    //判断点击富文本内容为img图片
    if (imageUrls.length === 0) {
      return
    }
    imageList.value = originalImageUrls
    imageUrls.forEach((item: string, index: number) => {
      if (element.target.currentSrc.includes(item)) {
        currentImageIndex.value = index
      }
    })
    // 获取可视窗口的宽度
    var viewportWidth =
      window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
    if (viewportWidth > 768) {
      isShow.value = true
    } else {
      // 移动端
      showImagePreview({
        images: originalImageUrls,
        closeable: true,
        startPosition: currentImageIndex.value
      })
    }
  }
}
defineExpose({ showImage })
</script>
