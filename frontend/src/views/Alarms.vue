<template>
  <div>
    <h2 style="margin-bottom: 20px">报警记录</h2>
    <el-card shadow="hover">
      <el-table :data="alarms" style="width: 100%" border>
        <el-table-column prop="alarmId" label="报警ID" width="280" show-overflow-tooltip />
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="alarmType" label="报警类型" width="120">
          <template #default="scope">
            <el-tag type="danger">{{ scope.row.alarmType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="alarmTime" label="报警时间" width="180" />
        <el-table-column label="处理状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.handled ? 'success' : 'warning'">
              {{ scope.row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const alarms = ref([])

const loadAlarms = async () => {
  try {
    const res = await api.getAlarms()
    if (res.data.code === 200) {
      alarms.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载报警记录失败')
  }
}

onMounted(() => {
  loadAlarms()
})
</script>
