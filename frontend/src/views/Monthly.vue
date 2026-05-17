<template>
  <div>
    <h2 style="margin-bottom: 20px">月租管理</h2>

    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>添加月租车辆</span>
        </div>
      </template>
      <el-form :model="monthlyForm" label-width="100px" inline>
        <el-form-item label="车牌号">
          <el-input v-model="monthlyForm.licensePlate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车主姓名">
          <el-input v-model="monthlyForm.ownerName" placeholder="请输入车主姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="monthlyForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="monthlyForm.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="monthlyForm.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addMonthlyVehicle">添加</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <span>月租车辆列表</span>
      </template>
      <el-table :data="monthlyVehicles" style="width: 100%" border>
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="ownerName" label="车主姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="startDate" label="开始日期" width="150" />
        <el-table-column prop="endDate" label="结束日期" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="new Date(scope.row.endDate) < new Date()" type="danger">已过期</el-tag>
            <el-tag v-else-if="new Date(scope.row.endDate) < new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)" type="warning">即将到期</el-tag>
            <el-tag v-else type="success">正常</el-tag>
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

const monthlyVehicles = ref([])
const monthlyForm = ref({
  licensePlate: '',
  ownerName: '',
  phone: '',
  startDate: new Date(),
  endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
})

const loadMonthlyVehicles = async () => {
  try {
    const res = await api.getMonthlyVehicles()
    if (res.data.code === 200) {
      monthlyVehicles.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载月租车辆失败')
  }
}

const addMonthlyVehicle = async () => {
  if (!monthlyForm.value.licensePlate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    const data = {
      ...monthlyForm.value,
      active: true
    }
    const res = await api.addMonthlyVehicle(data)
    if (res.data.code === 200) {
      ElMessage.success('添加成功')
      monthlyForm.value = {
        licensePlate: '',
        ownerName: '',
        phone: '',
        startDate: new Date(),
        endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
      loadMonthlyVehicles()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

onMounted(() => {
  loadMonthlyVehicles()
})
</script>
