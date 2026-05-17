<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #2e3b4e">
      <div style="color: white; text-align: center; padding: 20px 0; font-size: 18px; font-weight: bold">
        停车场管理系统
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#2e3b4e"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/overview">
          <el-icon><DataAnalysis /></el-icon>
          <span>系统概览</span>
        </el-menu-item>
        <el-menu-item index="/entry-exit">
          <el-icon><Van /></el-icon>
          <span>出入场管理</span>
        </el-menu-item>
        <el-menu-item index="/spots">
          <el-icon><Grid /></el-icon>
          <span>车位管理</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Document /></el-icon>
          <span>停车记录</span>
        </el-menu-item>
        <el-menu-item index="/monthly">
          <el-icon><User /></el-icon>
          <span>月租管理</span>
        </el-menu-item>
        <el-menu-item index="/blacklist">
          <el-icon><Lock /></el-icon>
          <span>黑名单管理</span>
        </el-menu-item>
        <el-menu-item index="/alarms">
          <el-icon><Warning /></el-icon>
          <span>报警记录</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>统计报表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: flex-end; padding-right: 20px">
        <span style="color: #666">管理员</span>
      </el-header>
      <el-main style="background-color: #f5f7fa">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, Van, Grid, Document, User, Lock, Warning, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
