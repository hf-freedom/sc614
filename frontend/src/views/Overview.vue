<template>
  <div>
    <h2 style="margin-bottom: 20px">系统概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #409EFF; font-weight: bold">{{ overview.totalSpots || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">总车位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #E6A23C; font-weight: bold">{{ overview.occupiedSpots || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">已占用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #67C23A; font-weight: bold">{{ overview.availableSpots || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">空闲车位</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #F56C6C; font-weight: bold">{{ overview.occupancyRate || '0%' }}</div>
            <div style="color: #909399; margin-top: 10px">占用率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #67C23A; font-weight: bold">¥{{ overview.todayRevenue || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">今日收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #409EFF; font-weight: bold">{{ overview.todayEntries || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">今日入场</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="font-size: 36px; color: #E6A23C; font-weight: bold">{{ overview.todayExits || 0 }}</div>
            <div style="color: #909399; margin-top: 10px">今日出场</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>分区空闲车位</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(count, zone) in overview.spotsByZone" :key="zone">
          <div style="text-align: center; padding: 20px; background: #f5f7fa; border-radius: 8px">
            <div style="font-size: 24px; font-weight: bold; color: #409EFF">{{ zone }}区</div>
            <div style="font-size: 28px; color: #67C23A; margin-top: 10px">{{ count }}个</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>即将到期月租车辆</span>
      </template>
      <el-table :data="expiringVehicles" style="width: 100%">
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="ownerName" label="车主姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="endDate" label="到期日期" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag type="warning">即将到期</el-tag>
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

const overview = ref({})
const expiringVehicles = ref([])

const loadData = async () => {
  try {
    const res = await api.getOverview()
    if (res.data.code === 200) {
      overview.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载概览数据失败')
  }

  try {
    const res = await api.getExpiringVehicles()
    if (res.data.code === 200) {
      expiringVehicles.value = res.data.data
    }
  } catch (error) {
    console.error('加载即将到期车辆失败')
  }
}

onMounted(() => {
  loadData()
})
</script>
