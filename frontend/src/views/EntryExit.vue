<template>
  <div>
    <h2 style="margin-bottom: 20px">出入场管理</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="车辆入场" name="entry">
        <el-card shadow="hover">
          <el-form :model="entryForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="车牌号">
              <el-input v-model="entryForm.licensePlate" placeholder="请输入车牌号" />
            </el-form-item>
            <el-form-item label="车辆类型">
              <el-select v-model="entryForm.vehicleType" placeholder="请选择车辆类型">
                <el-option label="临时车" value="TEMPORARY" />
                <el-option label="月租车" value="MONTHLY" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleEntry">确认入场</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="费用查询与支付" name="pay">
        <el-card shadow="hover">
          <el-alert
            v-if="paidSuccess"
            title="支付成功"
            type="success"
            :closable="false"
            style="margin-bottom: 20px"
          >
            <template #default>
              <div>车牌号：{{ paidLicensePlate }}</div>
              <div>支付时间：{{ paidTime }}</div>
              <div style="font-weight: bold; color: #E6A23C">请在 15 分钟内完成出场，超时将产生额外费用</div>
            </template>
          </el-alert>
          <el-form :model="payForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="车牌号">
              <el-input v-model="payForm.licensePlate" placeholder="请输入车牌号" />
              <el-button type="primary" @click="calculateFee" style="margin-left: 10px">查询费用</el-button>
            </el-form-item>
            <el-form-item label="停车费用" v-if="currentFee !== null">
              <div style="font-size: 24px; color: #F56C6C; font-weight: bold">¥{{ currentFee }}</div>
            </el-form-item>
            <el-form-item v-if="currentFee !== null">
              <el-button type="success" @click="handlePay">确认支付</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="车辆出场" name="exit">
        <el-card shadow="hover">
          <el-alert
            v-if="paidSuccess"
            title="已支付提示"
            type="info"
            :closable="false"
            style="margin-bottom: 20px"
          >
            <template #default>
              <div>车辆 {{ paidLicensePlate }} 已于 {{ paidTime }} 完成支付</div>
              <div style="font-weight: bold; color: #E6A23C">请及时出场，15分钟后将产生超时费用</div>
              <el-button type="primary" size="small" style="margin-top: 10px" @click="fillExitPlate">快速填入出场车牌号</el-button>
            </template>
          </el-alert>
          <el-form :model="exitForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="车牌号">
              <el-input v-model="exitForm.licensePlate" placeholder="请输入车牌号" />
            </el-form-item>
            <el-form-item label="强制出场">
              <el-switch v-model="exitForm.forceExit" />
              <span style="color: #909399; margin-left: 10px">（支付超时后需强制出场，将收取超时费用）</span>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="handleExit">确认出场</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('entry')
const entryForm = ref({ licensePlate: '', vehicleType: 'TEMPORARY' })
const payForm = ref({ licensePlate: '' })
const exitForm = ref({ licensePlate: '', forceExit: false })
const currentFee = ref(null)
const paidSuccess = ref(false)
const paidLicensePlate = ref('')
const paidTime = ref('')

const formatTime = (date) => {
  const pad = (n) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const handleEntry = async () => {
  if (!entryForm.value.licensePlate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    const res = await api.vehicleEntry(entryForm.value)
    if (res.data.code === 200) {
      ElMessage.success('入场成功，分配车位：' + res.data.data.spotId)
      entryForm.value.licensePlate = ''
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('入场失败')
  }
}

const calculateFee = async () => {
  if (!payForm.value.licensePlate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    const res = await api.calculateFee(payForm.value.licensePlate)
    if (res.data.code === 200) {
      currentFee.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('查询费用失败')
  }
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm(`确认支付停车费 ¥${currentFee.value}？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.payFee({ licensePlate: payForm.value.licensePlate, amount: currentFee.value })
    if (res.data.code === 200) {
      paidSuccess.value = true
      paidLicensePlate.value = payForm.value.licensePlate
      paidTime.value = formatTime(new Date())
      currentFee.value = null
      payForm.value.licensePlate = ''
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

const fillExitPlate = () => {
  exitForm.value.licensePlate = paidLicensePlate.value
  activeTab.value = 'exit'
}

const handleExit = async () => {
  if (!exitForm.value.licensePlate) {
    ElMessage.warning('请输入车牌号')
    return
  }
  try {
    const res = await api.vehicleExit(exitForm.value)
    if (res.data.code === 200) {
      ElMessage.success('出场成功')
      exitForm.value.licensePlate = ''
      exitForm.value.forceExit = false
      if (paidLicensePlate.value && exitForm.value.licensePlate === paidLicensePlate.value) {
        paidSuccess.value = false
        paidLicensePlate.value = ''
        paidTime.value = ''
      }
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('出场失败')
  }
}
</script>
