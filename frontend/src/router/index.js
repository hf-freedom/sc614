import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../views/Overview.vue'
import EntryExit from '../views/EntryExit.vue'
import Spots from '../views/Spots.vue'
import Records from '../views/Records.vue'
import Monthly from '../views/Monthly.vue'
import Blacklist from '../views/Blacklist.vue'
import Alarms from '../views/Alarms.vue'
import Statistics from '../views/Statistics.vue'

const routes = [
  { path: '/', redirect: '/overview' },
  { path: '/overview', component: Overview, meta: { title: '概览' } },
  { path: '/entry-exit', component: EntryExit, meta: { title: '出入场管理' } },
  { path: '/spots', component: Spots, meta: { title: '车位管理' } },
  { path: '/records', component: Records, meta: { title: '停车记录' } },
  { path: '/monthly', component: Monthly, meta: { title: '月租管理' } },
  { path: '/blacklist', component: Blacklist, meta: { title: '黑名单管理' } },
  { path: '/alarms', component: Alarms, meta: { title: '报警记录' } },
  { path: '/statistics', component: Statistics, meta: { title: '统计报表' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
