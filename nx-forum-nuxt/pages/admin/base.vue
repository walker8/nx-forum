<template>
  <div class="nx-container">
    <el-card shadow="hover">
      <el-form :model="websiteBaseInfo" label-width="auto" :rules="rules" ref="formRef">
        <el-form-item prop="websiteName" label="网站名称">
          <el-input
            v-model.trim="websiteBaseInfo.websiteName"
            placeholder="请输入网站名称"
            style="max-width: 300px"
          />
        </el-form-item>
        <el-form-item prop="websiteIntroduction" label="网站介绍">
          <el-input
            v-model="websiteBaseInfo.websiteIntroduction"
            placeholder="请输入网站介绍"
            type="textarea"
            style="max-width: 600px"
            :autosize="{ minRows: 2, maxRows: 5 }"
          />
        </el-form-item>
        <el-form-item prop="seoTitle" label="SEO标题">
          <el-input
            v-model="websiteBaseInfo.seoTitle"
            placeholder="SEO标题，如果设置会代替版块名称，不超过60个字符"
            style="max-width: 600px"
          />
        </el-form-item>
        <el-form-item prop="seoKeywords" label="SEO关键字">
          <el-input
            v-model="websiteBaseInfo.seoKeywords"
            placeholder="SEO关键字，不要超过6个，关键字之间用英文的,隔开"
            style="max-width: 600px"
          />
        </el-form-item>
        <el-form-item prop="seoContent" label="SEO内容">
          <el-input
            v-model="websiteBaseInfo.seoContent"
            placeholder="SEO内容，有助于搜索引擎收录，不超过180个字符"
            type="textarea"
            style="max-width: 600px"
            :autosize="{ minRows: 2, maxRows: 4 }"
          />
        </el-form-item>
        <div class="el-form-item">
          <el-button type="primary" @click="onSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getWebsiteBaseInfoByAdmin,
  updateWebsiteBaseInfoByAdmin,
  type WebsiteBaseInfoVO
} from '~/apis/config'

definePageMeta({
  layout: 'admin'
})

const formRef = ref<FormInstance>()
const websiteBaseInfo = reactive({
  websiteName: '',
  websiteIntroduction: '',
  seoTitle: '',
  seoContent: '',
  seoKeywords: ''
})
const rules = reactive<FormRules<WebsiteBaseInfoVO>>({
  websiteName: [{ required: true, message: '请填写网站名称', trigger: 'blur' }]
})
getWebsiteBaseInfoByAdmin()
  .then((res) => {
    Object.assign(websiteBaseInfo, res.data)
  })
  .catch((error) => {
    ElMessage.error(error || '获取网站基本信息失败')
  })
const onSubmit = () => {
  formRef.value?.validate((valid, fields) => {
    if (valid) {
      updateWebsiteBaseInfoByAdmin(websiteBaseInfo).then((res) => {
        ElMessage.success(res.data)
      })
    }
  })
}
</script>
