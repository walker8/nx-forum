<template>
  <el-card>
    <el-form :model="profile" label-width="auto" ref="formRef">
      <el-form-item prop="avatar" label="头像">
        <van-uploader
          v-model="fileList"
          multiple
          :max-count="1"
          :max-size="10 * 1024 * 1024"
          @oversize="onOversize"
          result-type="file"
          :preview-full-image="false"
          show-upload
          upload-text="点击上传图片"
        />
      </el-form-item>
      <el-form-item prop="userName" label="用户名称">
        <el-input
          v-model.trim="profile.userName"
          placeholder="请输入用户名称"
          style="max-width: 300px"
          show-word-limit
          maxlength="10"
        />
      </el-form-item>
      <el-form-item prop="intro" label="个人介绍">
        <el-input
          v-model="profile.intro"
          placeholder="请填写职业技能，擅长的事情，兴趣爱好等"
          type="textarea"
          style="max-width: 600px"
          :autosize="{ minRows: 3, maxRows: 5 }"
          show-word-limit
          maxlength="100"
        />
      </el-form-item>
      <div class="el-form-item">
        <el-button type="primary" @click="onSubmit">保存修改</el-button>
      </div>
    </el-form>
  </el-card>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { UploaderFileListItem } from 'vant'
import { uploadAvatar } from '~/apis/image'
import { getCurrentUser, updateCurrentUser } from '~/apis/uc/user'

definePageMeta({
  layout: 'user-setting'
})
useSeoMeta({
  title: '个人资料'
})
const profile = reactive({
  userName: '',
  intro: ''
})
if (import.meta.client) {
  // 只在客户端执行代码
  nextTick(() => {
    const token = useCookie('x_token')
    if (token.value) {
      getCurrentUser().then((res) => {
        const data = res.data
        if (data == null) {
          token.value = ''
          return
        }
        profile.userName = data.userName
        profile.intro = data.intro
        if (data.avatar) {
          fileList.value = [{ url: data.avatar, isImage: true }]
        }
      })
    }
  })
}
const onOversize = (file: UploaderFileListItem) => {
  showToast('图片大小不能超过 10mb')
}
const fileList = ref<Array<UploaderFileListItem>>([])
const onSubmit = async () => {
  await upload()
  if (fileList.value[0]?.status === 'failed') {
    // 头像上传失败
    return
  }
  const avatarUrl = fileList.value[0]?.url
  if (!avatarUrl) {
    ElMessage.error('用户头像不能为空，请上传图片！')
    return
  }
  updateCurrentUser({
    userName: profile.userName,
    intro: profile.intro,
    avatar: avatarUrl
  })
    .then((res) => {
      ElMessage.success(res.data)
    })
    .catch((err) => {
      ElMessage.error(err)
    })
}
const upload = async () => {
  for (let file of fileList.value) {
    if (file.file instanceof File) {
      file.status = 'uploading'
      const formData = new FormData()
      formData.append('file', file.file)
      try {
        const res = await uploadAvatar(formData)
        file.status = 'done'
        file.file = undefined
        file.url = res.data
      } catch (error) {
        file.status = 'failed'
        ElMessage.error(error)
      }
    }
  }
}
</script>
