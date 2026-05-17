<template>
  <div>
    <h2 style="margin-bottom: 20px">车位管理</h2>

    <el-card shadow="hover" style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 20px; flex-wrap: wrap">
        <div style="display: flex; align-items: center; gap: 8px">
          <div style="width: 20px; height: 20px; background: #67C23A; border-radius: 4px"></div>
          <span>空闲</span>
        </div>
        <div style="display: flex; align-items: center; gap: 8px">
          <div style="width: 20px; height: 20px; background: #E6A23C; border-radius: 4px"></div>
          <span>已占用</span>
        </div>
        <div style="display: flex; align-items: center; gap: 8px">
          <div style="width: 20px; height: 20px; background: #F56C6C; border-radius: 4px"></div>
          <span>异常</span>
        </div>
        <div style="color: #909399; margin-left: auto">
          💡 点击车位卡片可进行手动调整
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeZone">
      <el-tab-pane :label="zone + '区'" :name="zone" v-for="zone in zones" :key="zone">
        <div style="display: flex; flex-wrap: wrap; gap: 15px">
          <div
            v-for="spot in getZoneSpots(zone)"
            :key="spot.spotId"
            @click="handleSpotClick(spot)"
            style="width: 120px; height: 120px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1)"
            :style="getSpotStyle(spot)"
            @mouseenter="spot.hover = true"
            @mouseleave="spot.hover = false"
          >
            <div style="font-weight: bold; font-size: 18px">{{ spot.spotId }}</div>
            <div style="font-size: 13px; margin-top: 8px">{{ getSpotStatusText(spot) }}</div>
            <div v-if="spot.licensePlate && spot.occupied" style="font-size: 11px; margin-top: 4px; opacity: 0.9">{{ spot.licensePlate }}</div>
            <div v-if="spot.abnormalReason" style="font-size: 10px; margin-top: 4px; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ spot.abnormalReason }}</div>
            <div v-if="spot.hover" style="margin-top: 8px; font-size: 12px; background: rgba(0,0,0,0.2); padding: 2px 8px; border-radius: 10px">
              点击调整
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="adjustDialogVisible" title="管理员车位调整" width="600px">
      <el-alert
        title="管理员操作"
        type="warning"
        description="此操作将手动修改车位状态，请谨慎操作并填写调整原因"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="车位编号">
          <el-input v-model="adjustForm.spotId" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getCurrentStatusType()">
            {{ getCurrentStatusText() }}
          </el-tag>
        </el-form-item>
        <el-form-item label="当前车牌号" v-if="currentSpot?.licensePlate">
          <el-input :value="currentSpot.licensePlate" disabled />
        </el-form-item>
        <el-form-item label="设置状态为">
          <el-radio-group v-model="adjustForm.occupied">
            <el-radio :label="false">空闲</el-radio>
            <el-radio :label="true">占用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="车牌号" v-if="adjustForm.occupied">
          <el-input v-model="adjustForm.licensePlate" placeholder="请输入车牌号（占用时必填）" />
        </el-form-item>
        <el-form-item label="调整原因" required>
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细填写调整原因，如：系统异常、设备故障、特殊情况等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="adjustForm.operator" placeholder="请输入操作人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjust" :disabled="!adjustForm.reason">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const zones = ['A', 'B', 'C', 'D']
const activeZone = ref('A')
const spots = ref([])
const adjustDialogVisible = ref(false)
const currentSpot = ref(null)
const adjustForm = ref({ 
  spotId: '', 
  occupied: false, 
  licensePlate: '', 
  reason: '',
  operator: '' 
})

const getZoneSpots = (zone) => {
  return spots.value.filter(s => s.zone === zone)
}

const getSpotStyle = (spot) => {
  let baseStyle = {
    transform: spot.hover ? 'scale(1.05)' : 'scale(1)',
    boxShadow: spot.hover ? '0 4px 20px 0 rgba(0,0,0,0.2)' : '0 2px 12px 0 rgba(0,0,0,0.1)'
  }
  
  if (spot.abnormal) {
    return { ...baseStyle, background: 'linear-gradient(135deg, #F56C6C 0%, #c45656 100%)', color: 'white' }
  } else if (spot.occupied) {
    return { ...baseStyle, background: 'linear-gradient(135deg, #E6A23C 0%, #b88230 100%)', color: 'white' }
  } else {
    return { ...baseStyle, background: 'linear-gradient(135deg, #67C23A 0%, #529b2e 100%)', color: 'white' }
  }
}

const getSpotStatusText = (spot) => {
  if (spot.abnormal) return '异常'
  if (spot.occupied) return '已占用'
  return '空闲'
}

const getCurrentStatusType = () => {
  if (!currentSpot.value) return 'info'
  if (currentSpot.value.abnormal) return 'danger'
  if (currentSpot.value.occupied) return 'warning'
  return 'success'
}

const getCurrentStatusText = () => {
  if (!currentSpot.value) return '未知'
  if (currentSpot.value.abnormal) return '异常'
  if (currentSpot.value.occupied) return '已占用'
  return '空闲'
}

const handleSpotClick = (spot) => {
  currentSpot.value = spot
  adjustForm.value = {
    spotId: spot.spotId,
    occupied: spot.occupied,
    licensePlate: spot.licensePlate || '',
    reason: '',
    operator: ''
  }
  adjustDialogVisible.value = true
}

const handleAdjust = async () => {
  if (!adjustForm.value.reason) {
    ElMessage.warning('请填写调整原因')
    return
  }
  if (adjustForm.value.occupied && !adjustForm.value.licensePlate) {
    ElMessage.warning('设置为占用状态时，车牌号不能为空')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认将车位 ${adjustForm.value.spotId} 状态调整为 ${adjustForm.value.occupied ? '占用' : '空闲'}？\n此操作将被记录。`,
      '管理员操作确认',
      {
        confirmButtonText: '确认调整',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await api.adjustSpot(adjustForm.value)
    if (res.data.code === 200) {
      ElMessage.success('车位状态调整成功')
      adjustDialogVisible.value = false
      loadSpots()
    } else {
      ElMessage.error(res.data.message || '调整失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('调整失败')
    }
  }
}

const loadSpots = async () => {
  try {
    const res = await api.getSpots()
    if (res.data.code === 200) {
      spots.value = res.data.data.map(s => ({ ...s, hover: false }))
    }
  } catch (error) {
    ElMessage.error('加载车位数据失败')
  }
}

onMounted(() => {
  loadSpots()
})
</script>
