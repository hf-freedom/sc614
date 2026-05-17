<template>
  <div>
    <h2 style="margin-bottom: 20px">统计报表</h2>
    <el-alert
      title="统计说明"
      type="info"
      description="每日统计数据实时更新，空闲车位数量与系统概览保持一致，反映当前最新状态。"
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-card shadow="hover">
      <template #header>
        <span>每日统计</span>
      </template>
      <el-table :data="statistics" style="width: 100%" border>
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="totalRevenue" label="总收入(元)" width="120">
          <template #default="scope">
            <span style="color: #67C23A; font-weight: bold">¥{{ scope.row.totalRevenue || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="emptySpots" label="空闲车位" width="100" />
        <el-table-column prop="abnormalExits" label="异常出场数" width="120" />
        <el-table-column prop="totalEntries" label="总入场数" width="100" />
        <el-table-column prop="totalExits" label="总出场数" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const statistics = ref([])

const loadStatistics = async () => {
  try {
    const res = await api.getStatistics()
    if (res.data.code === 200) {
      statistics.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>
