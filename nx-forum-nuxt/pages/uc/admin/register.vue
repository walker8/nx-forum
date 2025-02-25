<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules, InputInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getRegisterConfig, updateRegisterConfig, type RegisterConfigDTO } from '@/apis/uc/register'

definePageMeta({
  layout: 'uc'
})

const formRef = ref<FormInstance>()

const form = reactive<RegisterConfigDTO>({
  enableRegister: true,
  enableSmsRegister: false,
  enableEmailRegister: true,
  enableRegisterCaptcha: true,
  forbiddenUsernames: [],
  passwordMinLength: 6,
  passwordMaxLength: 20,
  passwordStrength: 1
})

const rules = reactive<FormRules>({
  passwordMinLength: [
    { required: true, message: '请输入密码最小长度', trigger: 'blur' },
    { type: 'number', min: 6, max: 20, message: '长度范围为6-20', trigger: 'blur' }
  ],
  passwordMaxLength: [
    { required: true, message: '请输入密码最大长度', trigger: 'blur' },
    { type: 'number', min: 6, max: 20, message: '长度范围为6-20', trigger: 'blur' }
  ]
})

const getConfig = async () => {
  try {
    const res = await getRegisterConfig()
    Object.assign(form, res.data)
  } catch (error) {
    ElMessage.error(error || '获取配置失败')
  }
}

const saveConfig = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await updateRegisterConfig(form)
      ElMessage.success('保存成功')
    } catch (error) {
      ElMessage.error(error || '保存失败')
    }
  })
}

// 禁止的用户名输入控制
const inputVisible = ref(false)
const inputValue = ref('')
const inputRef = ref<InputInstance>()

const handleClose = (username: string) => {
  form.forbiddenUsernames = form.forbiddenUsernames.filter((name) => name !== username)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    inputRef.value?.input?.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value && !form.forbiddenUsernames.includes(inputValue.value)) {
    form.forbiddenUsernames.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

onMounted(() => {
  getConfig()
})
</script>

<template>
  <el-card shadow="never">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="160px">
      <el-form-item label="开启注册">
        <el-switch v-model="form.enableRegister" />
      </el-form-item>

      <el-form-item label="注册方式">
        <el-checkbox v-model="form.enableEmailRegister">邮箱注册</el-checkbox>
        <el-checkbox v-model="form.enableSmsRegister">短信注册</el-checkbox>
      </el-form-item>

      <el-form-item label="开启注册验证码">
        <el-switch v-model="form.enableRegisterCaptcha" />
      </el-form-item>

      <el-form-item label="密码长度限制">
        <div class="flex items-center gap-2">
          <el-input-number v-model="form.passwordMinLength" :min="6" :max="20" class="!w-[120px]" />
          <el-text>至</el-text>
          <el-input-number v-model="form.passwordMaxLength" :min="6" :max="20" class="!w-[120px]" />
          <el-text>个字符</el-text>
        </div>
      </el-form-item>

      <el-form-item label="密码强度要求">
        <el-radio-group v-model="form.passwordStrength">
          <el-radio :value="0">无要求</el-radio>
          <el-radio :value="1">必须包含数字和字母</el-radio>
          <el-radio :value="2">必须包含数字、字母和特殊字符</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="禁止的用户名">
        <div class="mb-2">
          <el-tag
            v-for="username in form.forbiddenUsernames"
            :key="username"
            closable
            type="danger"
            :disable-transitions="false"
            @close="handleClose(username)"
          >
            {{ username }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="inputRef"
            v-model="inputValue"
            class="input-new-tag"
            size="small"
            @keyup.enter="handleInputConfirm"
            @blur="handleInputConfirm"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showInput">
            + 添加用户名
          </el-button>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="saveConfig">保存设置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>
