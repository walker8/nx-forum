<template>
  <el-dialog
    v-model="dialogVisible"
    :title="form.userId > 0 ? '编辑用户' : '新增用户'"
    style="max-width: 450px"
  >
    <el-form :model="form" label-width="80px" ref="formRef" :rules="rules" v-if="dialogVisible">
      <el-form-item label="用户ID" v-show="form.userId > 0" prop="userId">
        <el-input v-model="form.userId" :disabled="true" />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" show-password placeholder="请输入密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">确定</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { saveUserByAdmin, updateUserByAdmin, type UserCmd } from '~/apis/uc/user'
const formRef = ref<FormInstance>()
const dialogVisible = ref(false)
const form = reactive({
  userName: '',
  userId: 0,
  password: '',
  email: ''
})
const rules = reactive<FormRules>({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 16, message: '长度在 3 到 16 个字符', trigger: 'blur' }
  ],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }]
})
const showDialog = (user: UserCmd | null) => {
  form.email = ''
  form.userName = ''
  form.password = ''
  form.userId = 0
  if (user?.userId && user?.userId > 0) {
    form.email = user.email || ''
    form.userId = user.userId || 0
    form.userName = user.userName || ''
  }
  if (form.userId > 0) {
    rules.password = [{ min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }]
  } else {
    rules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
    ]
  }
  dialogVisible.value = true
}
const emit = defineEmits(['refresh'])
const addUser = () => {
  saveUserByAdmin(form)
    .then(() => {
      dialogVisible.value = false
      ElMessage.success('新增用户成功')
      emit('refresh')
    })
    .catch((errMsg) => {
      ElMessage.error(errMsg)
    })
}
const updateUser = () => {
  updateUserByAdmin(form)
    .then(() => {
      dialogVisible.value = false
      ElMessage.success('修改用户成功')
      emit('refresh')
    })
    .catch((errMsg) => {
      ElMessage.error(errMsg)
    })
}
const onSubmit = () => {
  formRef.value?.validate((valid) => {
    if (valid) {
      if (form.userId > 0) {
        updateUser()
      } else {
        addUser()
      }
    }
  })
}
defineExpose({ showDialog })
</script>
