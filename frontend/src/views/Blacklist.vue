<template>
  <div>
    <h2 style="margin-bottom: 20px">黑名单管理</h2>

    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <span>添加黑名单车辆</span>
      </template>
      <el-form :model="blacklistForm" label-width="100px" inline>
        <el-form-item label="车牌号">
          <el-input v-model="blacklistForm.licensePlate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="blacklistForm.reason" placeholder="请输入拉黑原因" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="addToBlacklist">加入黑名单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <span>黑名单列表</span>
      </template>
      <el-table :data="blacklist" style="width: 100%" border>
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="reason" label="拉黑原因" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.blocked">已拉黑</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" link @click="removeFromBlacklist(scope.row.licensePlate)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const blacklist = ref([])
const blacklistForm = ref({
  licensePlate: '',
  reason: ''
})

const loadBlacklist = async () => {
  try {
    const res = await api.getBlacklist()
    if (res.data.code === 200) {
      blacklist.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载黑名单失败')
  }
}

const addToBlacklist = async () => {
  if (!blacklistForm.value.licensePlate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    const res = await api.addToBlacklist(blacklistForm.value)
    if (res.data.code === 200) {
      ElMessage.success('已加入黑名单')
      blacklistForm.value.licensePlate = ''
      blacklistForm.value.reason = ''
      loadBlacklist()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const removeFromBlacklist = async (licensePlate) => {
  try {
    await ElMessageBox.confirm(`确认将 ${licensePlate} 移出黑名单？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.removeFromBlacklist(licensePlate)
    if (res.data.code === 200) {
      ElMessage.success('已移除')
      loadBlacklist()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

onMounted(() => {
  loadBlacklist()
})
</script>
