<template>
  <div class="nx-container">
    <el-card shadow="never">
      <div class="search-wrapper">
        <el-form ref="searchFormRef" :inline="true" :model="searchData" :label-width="84">
          <el-form-item prop="imageType" label="图片类型">
            <el-select
              style="width: 150px"
              v-model="searchData.imageType"
              placeholder="请选择图片类型"
              clearable
            >
              <el-option label="帖子图片" :value="0" />
              <el-option label="用户头像" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item prop="storageType" label="存储类型">
            <el-select
              style="width: 150px"
              v-model="searchData.storageType"
              placeholder="请选择存储类型"
              clearable
            >
              <el-option label="本地存储" :value="0" />
              <el-option label="阿里云OSS" :value="1" />
              <el-option label="腾讯云COS" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item prop="createBy" label="上传用户">
            <el-autocomplete
              v-model="searchData.userName"
              :fetch-suggestions="querySearchUser"
              placeholder="请输入用户名"
              @select="(item) => handleSearchUserSelect(item as UserVO)"
              value-key="userName"
              style="width: 200px"
            />
          </el-form-item>
          <div class="el-form-item">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </div>
        </el-form>
      </div>

      <div class="toolbar-wrapper">
        <div>
          <el-button type="danger" size="default" @click="batchDelete">批量删除</el-button>
          <el-button-group class="ml-4">
            <el-tooltip content="表格视图">
              <el-button
                :type="viewMode === 'table' ? 'primary' : ''"
                :icon="List"
                @click="viewMode = 'table'"
              />
            </el-tooltip>
            <el-tooltip content="瀑布流视图">
              <el-button
                :type="viewMode === 'waterfall' ? 'primary' : ''"
                :icon="Grid"
                @click="viewMode = 'waterfall'"
              />
            </el-tooltip>
          </el-button-group>
        </div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button
              type="primary"
              :icon="RefreshRight"
              circle
              @click="getTableData"
              size="small"
            />
          </el-tooltip>
        </div>
      </div>

      <div v-if="viewMode === 'table'" class="table-wrapper">
        <el-table
          :data="tableData"
          v-loading="loading"
          border
          ref="tableRef"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="imagePath" label="图片预览" width="120" align="center">
            <template #default="scope">
              <el-image :src="scope.row.imagePath" fit="cover" class="w-20 h-20 object-cover">
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="imagePath" label="图片路径" min-width="300" />
          <el-table-column prop="imageType" label="图片类型" width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.imageType === 0 ? 'primary' : 'success'">
                {{ scope.row.imageType === 0 ? '帖子图片' : '用户头像' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="文件大小" width="120" align="center">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="storageType" label="存储类型" width="120" align="center">
            <template #default="scope">
              <el-tag :type="getStorageTypeTag(scope.row.storageType)">
                {{ getStorageTypeName(scope.row.storageType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="上传用户" width="120" align="center" />
          <el-table-column prop="createTime" label="上传时间" width="160" align="center" />
          <el-table-column fixed="right" label="操作" width="120" align="center">
            <template #default="scope">
              <el-button
                type="danger"
                text
                bg
                size="small"
                @click="handleDelete(scope.row.imageId)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else class="waterfall-wrapper">
        <el-checkbox-group v-model="selectedIds" class="image-list">
          <div v-for="item in tableData" :key="item.imageId" class="image-item">
            <el-checkbox :value="item.imageId" class="image-checkbox" />
            <div class="type-tag">
              <el-tag size="small" :type="item.imageType === 0 ? 'primary' : 'success'">
                {{ item.imageType === 0 ? '帖子图片' : '用户头像' }}
              </el-tag>
            </div>
            <el-image
              :src="item.imagePath"
              :preview-src-list="[item.imagePath]"
              fit="cover"
              class="image"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="user-info">
              <span class="username">{{ item.authorName }}</span>
              <span class="create-time">{{ item.createTime }}</span>
            </div>
            <div class="image-overlay">
              <div class="info-tags">
                <el-tag size="small" type="info" class="file-size">
                  {{ formatFileSize(item.fileSize) }}
                </el-tag>
                <el-tag size="small" type="info" class="file-ext">
                  {{ item.fileExt.toUpperCase() }}
                </el-tag>
              </div>
              <el-button type="danger" text bg size="small" @click="handleDelete(item.imageId)">
                删除
              </el-button>
            </div>
          </div>
        </el-checkbox-group>
      </div>

      <div class="pager-wrapper">
        <el-pagination
          background
          :layout="paginationData.layout"
          :page-sizes="paginationData.pageSizes"
          :total="paginationData.total"
          :page-size="paginationData.pageSize"
          :currentPage="paginationData.currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Refresh, RefreshRight, Search, Picture, Grid, List } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'
import { queryPage, deleteImage } from '~/apis/image'

definePageMeta({
  layout: 'admin'
})

const searchData = reactive({
  imageType: undefined as number | undefined,
  storageType: undefined as number | undefined,
  createBy: undefined as number | undefined,
  userName: ''
})

interface ImageItem {
  imageId: number
  imagePath: string
  imageType: number
  fileSize: number
  fileExt: string
  storageType: number
  authorName: string
  createTime: string
}

const tableData = ref<ImageItem[]>([])
const loading = ref(false)
const tableRef = ref()
const selectedIds = ref<number[]>([])

const { paginationData, handleCurrentChange, handleSizeChange } = usePagination({ pageSize: 50 })

// 在 setup 中添加视图模式状态
const viewMode = ref<'table' | 'waterfall'>('table')

// 获取表格数据
const getTableData = () => {
  loading.value = true
  queryPage({
    pageNo: paginationData.currentPage,
    pageSize: paginationData.pageSize,
    imageType: searchData.imageType,
    storageType: searchData.storageType,
    createBy: searchData.createBy
  })
    .then((res) => {
      tableData.value = res.data.records
      paginationData.total = res.data.total
    })
    .catch((err) => {
      ElMessage.error(err)
      tableData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

// 搜索相关
const handleSearch = () => {
  paginationData.currentPage = 1
  getTableData()
}

const resetSearch = () => {
  searchData.imageType = undefined
  searchData.storageType = undefined
  searchData.createBy = undefined
  searchData.userName = ''
  handleSearch()
}

// 用户搜索
const querySearchUser = async (query: string) => {
  if (!query) return []
  try {
    const res = await queryUsername(query)
    return (res.data || []).map((item: UserVO) => ({
      value: item.userName,
      ...item
    }))
  } catch (error) {
    return []
  }
}

const handleSearchUserSelect = (item: UserVO) => {
  searchData.createBy = item.userId
  searchData.userName = item.userName
}

// 文件大小格式化
const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
}

// 存储类型相关
const getStorageTypeName = (type: number) => {
  switch (type) {
    case 0:
      return '本地存储'
    case 1:
      return '阿里云OSS'
    case 2:
      return '腾讯云COS'
    default:
      return '未知'
  }
}

const getStorageTypeTag = (type: number) => {
  switch (type) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'success'
    default:
      return 'info'
  }
}

// 选择相关
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item) => item.imageId)
}

// 删除单张图片
const handleDelete = async (imageId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该图片吗？', '提示', {
      type: 'warning'
    })
    await deleteImage([imageId]) // 修改为传入数组
    ElMessage.success('删除成功')
    getTableData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error || '删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的图片')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 张图片吗？`, '提示', {
      type: 'warning'
    })

    // 调用批量删除接口
    await deleteImage(selectedIds.value)
    ElMessage.success('批量删除成功')
    getTableData()
    selectedIds.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error || '批量删除失败')
    }
  }
}

// 监听分页变化
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, {
  immediate: true
})
</script>

<style lang="scss" scoped>
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.pager-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}

:deep(.el-image) {
  border-radius: 4px;
  overflow: hidden;
}

.table-wrapper {
  margin-bottom: 20px;
}

.waterfall-wrapper {
  margin-bottom: 20px;

  .image-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
    padding: 16px 0;
  }

  .image-item {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .image-checkbox {
      position: absolute;
      top: 8px;
      left: 8px;
      z-index: 2;
    }

    .type-tag {
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 2;

      :deep(.el-tag) {
        background-color: rgba(255, 255, 255, 0.9);
        border: none;
      }
    }

    .image {
      width: 100%;
      height: 200px;
      display: block;

      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .user-info {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 8px;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
      display: flex;
      flex-direction: column;
      gap: 4px;
      z-index: 1;

      .username {
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .create-time {
        opacity: 0.8;
      }
    }

    .image-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 8px;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 8px;
      z-index: 1;

      .info-tags {
        display: flex;
        gap: 4px;
        flex-wrap: wrap;

        :deep(.el-tag) {
          background-color: rgba(0, 0, 0, 0.6);
          border: none;
          color: #fff;
          backdrop-filter: blur(4px);

          &.file-size {
            font-family: monospace;
          }

          &.file-ext {
            text-transform: uppercase;
          }
        }
      }

      :deep(.el-button) {
        padding: 4px 8px;
        background-color: rgba(255, 255, 255, 0.9);
        border: none;
        align-self: flex-start;

        &:hover {
          background-color: rgba(255, 255, 255, 1);
        }
      }
    }

    &:hover {
      .image-overlay {
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.4));
      }
    }
  }
}

.text-sm {
  font-size: 0.8em;
}
</style>
