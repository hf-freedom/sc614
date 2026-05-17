<template>
  <div>
    <h2 style="margin-bottom: 20px">停车记录</h2>
    <el-card shadow="hover">
      <el-table :data="records" style="width: 100%" border>
        <el-table-column prop="recordId" label="记录ID" width="280" show-overflow-tooltip />
        <el-table-column prop="licensePlate" label="车牌号" width="120" />
        <el-table-column prop="vehicleType" label="车辆类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.vehicleType === 'MONTHLY' ? 'primary' : 'success'">
              {{ scope.row.vehicleType === 'MONTHLY' ? '月租车' : '临时车' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spotId" label="车位号" width="100" />
        <el-table-column prop="entryTime" label="入场时间" width="180" />
        <el-table-column prop="exitTime" label="出场时间" width="180" />
        <el-table-column prop="totalFee" label="费用(元)" width="100">
          <template #default="scope">
            <span v-if="scope.row.totalFee !== null">¥{{ scope.row.totalFee }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="paid" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.paid ? 'success' : 'warning'">
              {{ scope.row.paid ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exited" label="出场状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.exited ? 'info' : 'danger'">
              {{ scope.row.exited ? '已出场' : '在场内' }}
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

const records = ref([])

const loadRecords = async () => {
  try {
    const res = await api.getRecords()
    if (res.data.code === 200) {
      records.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载停车记录失败')
  }
}

onMounted(() => {
  loadRecords()
})
</script>
