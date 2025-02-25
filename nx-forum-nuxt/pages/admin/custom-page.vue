<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="flex justify-between mb-5">
        <div>
          <el-button type="primary" @click="handleCreate">新建页面</el-button>
        </div>
        <div>
          <el-tooltip content="刷新">
            <el-button :icon="RefreshRight" circle @click="fetchPages" />
          </el-tooltip>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="pageName" label="页面名称" />
        <el-table-column prop="pageCode" label="页面编码" />
        <el-table-column prop="accessLevel" label="访问权限" width="120">
          <template #default="{ row }">
            <el-tag :type="accessLevelMap[row.accessLevel]?.type">
              {{ accessLevelMap[row.accessLevel]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pageStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.pageStatus"
              :active-value="0"
              :inactive-value="1"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditContent(row.pageId)">
              编辑内容
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row.pageId)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-5 flex justify-end"
        background
        :layout="paginationData.layout"
        :page-sizes="paginationData.pageSizes"
        :total="paginationData.total"
        :page-size="paginationData.pageSize"
        :currentPage="paginationData.currentPage"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="页面名称" prop="pageName">
          <el-input v-model="formData.pageName" placeholder="请输入页面名称" />
        </el-form-item>
        <el-form-item label="页面编码" prop="pageCode">
          <el-input v-model="formData.pageCode" placeholder="请输入页面编码" />
        </el-form-item>
        <el-form-item label="访问权限" prop="accessLevel">
          <el-select v-model="formData.accessLevel" class="w-full">
            <el-option
              v-for="(item, key) in accessLevelMap"
              :key="key"
              :label="item.label"
              :value="Number(key)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="pageStatus">
          <el-switch v-model="formData.pageStatus" :active-value="0" :inactive-value="1" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { RefreshRight } from '@element-plus/icons-vue'
import { queryPagesByAdmin, updatePage, createPage, deletePage } from '~/apis/custom-page'
import { ElMessage } from 'element-plus'

definePageMeta({
  layout: 'admin'
})

const loading = ref(false)

interface AccessLevelItem {
  label: string
  type: 'success' | 'warning' | 'info' | 'primary' | 'danger'
}

interface AccessLevelMap {
  [key: number]: AccessLevelItem
}

const accessLevelMap: AccessLevelMap = {
  0: { label: '公开', type: 'success' },
  1: { label: '登录可见', type: 'warning' }
} as const

interface PageItem {
  pageId: number
  pageName: string
  pageCode: string
  accessLevel: 0 | 1 | 2
  pageStatus: number
}

const tableData = ref<PageItem[]>([])
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const currentPageId = ref<number | null>(null)

interface PageCmd {
  pageName: string
  pageCode: string
  accessLevel: number
  pageStatus: number
}

const formData = reactive<PageCmd>({
  pageName: '',
  pageCode: '',
  accessLevel: 0,
  pageStatus: 0
})

const rules = {
  pageName: [{ required: true, message: '请输入页面名称', trigger: 'blur' }],
  pageCode: [{ required: true, message: '请输入页面编码', trigger: 'blur' }],
  accessLevel: [{ required: true, message: '请选择访问权限', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑页面' : '新建页面'))

const fetchPages = async () => {
  try {
    loading.value = true
    const res = await queryPagesByAdmin({
      pageNo: paginationData.currentPage,
      pageSize: paginationData.pageSize,
      pageName: ''
    })
    tableData.value = res.data.records
    paginationData.total = res.data.total
  } catch (error) {
    ElMessage.error((error as Error)?.message || String(error))
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row: any) => {
  await updatePage(row.pageId, {
    ...row,
    pageStatus: row.pageStatus
  })
  ElMessage.success('状态更新成功')
}

const handleDelete = async (id: number) => {
  await deletePage(id)
  ElMessage.success('删除成功')
  fetchPages()
}

const handleCreate = () => {
  isEdit.value = false
  currentPageId.value = null
  Object.assign(formData, {
    pageName: '',
    pageCode: '',
    accessLevel: 0,
    pageStatus: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row: PageItem) => {
  isEdit.value = true
  currentPageId.value = row.pageId
  Object.assign(formData, {
    pageName: row.pageName,
    pageCode: row.pageCode,
    accessLevel: row.accessLevel,
    pageStatus: row.pageStatus
  })
  dialogVisible.value = true
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (isEdit.value && currentPageId.value) {
          await updatePage(currentPageId.value, formData)
        } else {
          await createPage(formData)
        }
        ElMessage.success(`${isEdit.value ? '更新' : '创建'}成功`)
        dialogVisible.value = false
        fetchPages()
      } catch (error) {
        ElMessage.error((error as Error)?.message || String(error))
      }
    }
  })
}

const handleEditContent = (pageId: number) => {
  navigateTo(`/editor/custom/${pageId}`)
}

onMounted(() => {
  fetchPages()
  watch([() => paginationData.currentPage, () => paginationData.pageSize], fetchPages, {
    immediate: false
  })
})
</script>
