<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Calendar,
  ChatDotRound,
  Check,
  Clock,
  CopyDocument,
  DataAnalysis,
  Document,
  Fold,
  Grid,
  MoreFilled,
  Plus,
  UploadFilled,
  Refresh,
  Search,
  Setting,
  TrendCharts,
  User,
  Warning,
} from '@element-plus/icons-vue'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
const OVERTIME_API =
  `${API_BASE_URL}/bd-bot/work-attendance-record/overtime/monthly`

const collapsed = ref(false)
const activeMenu = ref('overtime')
const search = ref('')
const dialogVisible = ref(false)

const navItems = [
  { id: 'overtime', label: '加班打卡记录', icon: Calendar },
  { id: 'overview', label: '数据概览', icon: Grid },
]

const statCards = [
  { label: '今日会话', value: '2,846', change: '+12.5%', icon: ChatDotRound, tone: 'violet' },
  { label: '活跃用户', value: '1,258', change: '+8.2%', icon: User, tone: 'blue' },
  { label: '问题解决率', value: '94.6%', change: '+3.1%', icon: Check, tone: 'green' },
  { label: '平均响应时间', value: '1.24s', change: '-0.18s', icon: TrendCharts, tone: 'orange' },
]

const bots = ref([
  { name: '售前咨询助手', desc: '产品介绍与购买引导', status: '运行中', sessions: 1280, rate: 96, updated: '刚刚' },
  { name: '客户服务助手', desc: '售后问题与工单处理', status: '运行中', sessions: 968, rate: 93, updated: '5 分钟前' },
  { name: '内部知识助手', desc: '企业知识检索与问答', status: '训练中', sessions: 421, rate: 88, updated: '20 分钟前' },
  { name: '活动推荐助手', desc: '个性化活动匹配', status: '已停用', sessions: 177, rate: 81, updated: '昨天' },
])

const filteredBots = computed(() => {
  const keyword = search.value.trim().toLowerCase()
  if (!keyword) return bots.value
  return bots.value.filter((bot) => `${bot.name}${bot.desc}`.toLowerCase().includes(keyword))
})

const chartPoints = [36, 48, 43, 66, 57, 78, 71, 90, 82, 101, 94, 116]
const areaPath = computed(() => {
  const width = 600
  const height = 176
  const max = Math.max(...chartPoints)
  const points = chartPoints.map((value, index) => {
    const x = (index / (chartPoints.length - 1)) * width
    const y = height - (value / max) * (height - 20)
    return [x, y]
  })
  const line = points.map(([x, y], i) => `${i ? 'L' : 'M'} ${x} ${y}`).join(' ')
  return { line, area: `${line} L ${width} ${height} L 0 ${height} Z`, points }
})

const attendanceLoading = ref(false)
const attendanceError = ref('')
const attendanceData = ref({ successCount: 0, summaries: [], errors: [] })
const selectedMonth = ref('')
const employeeKeyword = ref('')
const expandedEmployees = ref([])
const lastUpdated = ref('')
const selectedFile = ref(null)
const fileInput = ref(null)
const dragActive = ref(false)
const hasGenerated = ref(false)

const monthOptions = computed(() =>
  [...new Set(attendanceData.value.summaries.map((item) => item.month).filter(Boolean))].sort().reverse(),
)

const visibleSummaries = computed(() => {
  const keyword = employeeKeyword.value.trim().toLowerCase()
  return attendanceData.value.summaries.filter((item) => {
    const matchMonth = !selectedMonth.value || item.month === selectedMonth.value
    const matchName = !keyword || String(item.name || '').toLowerCase().includes(keyword)
    return matchMonth && matchName
  })
})

const attendanceStats = computed(() => {
  const summaries = visibleSummaries.value
  return {
    employees: summaries.length,
    records: summaries.reduce((sum, item) => sum + (item.details?.length || 0), 0),
    hours: summaries.reduce((sum, item) => sum + Number(item.totalOvertimeHours || 0), 0),
    overtimeDays: summaries.reduce(
      (sum, item) => sum + (item.details?.filter((detail) => Number(detail.overtimeMinutes) > 0).length || 0),
      0,
    ),
  }
})

const copyText = async (text, message = '已复制，可直接粘贴到 Excel') => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      textarea.remove()
    }
    ElMessage.success(message)
  } catch {
    ElMessage.error('复制失败，请手动选择内容复制')
  }
}

const batchOvertimeRows = computed(() =>
  visibleSummaries.value
    .flatMap((summary) => summary.details || [])
    .filter((detail) => Number(detail.overtimeMinutes || 0) > 0)
    .sort((a, b) => String(a.attendanceDate).localeCompare(String(b.attendanceDate))),
)

const copyBatchOvertime = () => {
  const content = batchOvertimeRows.value
    .map((detail) => `${detail.attendanceDate}\t${Number(detail.overtimeHours || 0).toFixed(1)}`)
    .join('\n')
  copyText(content, `已复制 ${batchOvertimeRows.value.length} 条加班记录，可直接粘贴到 Excel`)
}

const resetResult = () => {
  attendanceData.value = { successCount: 0, summaries: [], errors: [] }
  selectedMonth.value = ''
  employeeKeyword.value = ''
  expandedEmployees.value = []
  attendanceError.value = ''
  lastUpdated.value = ''
  hasGenerated.value = false
}

const selectFile = (file) => {
  if (!file) return
  const extension = file.name.split('.').pop()?.toLowerCase()
  if (!['xlsx', 'xls', 'csv'].includes(extension)) {
    ElMessage.warning('请选择 Excel 或 CSV 格式的考勤文件')
    return
  }
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 20MB')
    return
  }
  selectedFile.value = file
  resetResult()
}

const handleFileChange = (event) => {
  selectFile(event.target.files?.[0])
  event.target.value = ''
}

const handleDrop = (event) => {
  dragActive.value = false
  selectFile(event.dataTransfer?.files?.[0])
}

const removeFile = () => {
  selectedFile.value = null
  resetResult()
}

class BusinessError extends Error {}

const promptEmployeeName = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入员工姓名', '生成加班结果', {
      confirmButtonText: '开始生成',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：张三',
      inputValidator: (value) => Boolean(value?.trim()) || '请输入员工姓名',
    })
    return value.trim()
  } catch {
    return ''
  }
}

const generateAttendance = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择考勤文件')
    return
  }
  const employeeName = await promptEmployeeName()
  if (!employeeName) return
  attendanceLoading.value = true
  attendanceError.value = ''
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', employeeName)
    const response = await fetch(OVERTIME_API, {
      method: 'POST',
      headers: { Accept: 'application/json' },
      body: formData,
    })
    const payload = await response.json().catch(() => null)
    const responseErrors = Array.isArray(payload?.errors) ? payload.errors.filter(Boolean) : []
    const errorMessage = responseErrors.join('；')
    if (errorMessage) throw new BusinessError(errorMessage)
    if (!response.ok) throw new Error(errorMessage || `请求失败（HTTP ${response.status}）`)
    if (payload?.code != null && ![0, 200].includes(Number(payload.code))) {
      throw new BusinessError(errorMessage || `请求失败（业务码 ${payload.code}）`)
    }
    if (!payload || !Array.isArray(payload.summaries)) throw new Error('接口返回的数据格式不正确')
    attendanceData.value = {
      successCount: Number(payload.successCount || 0),
      summaries: payload.summaries,
      errors: Array.isArray(payload.errors) ? payload.errors : [],
    }
    hasGenerated.value = true
    if (!selectedMonth.value && monthOptions.value.length) selectedMonth.value = monthOptions.value[0]
    expandedEmployees.value = visibleSummaries.value.slice(0, 1).map((item) => `${item.name}-${item.month}`)
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  } catch (error) {
    if (error instanceof BusinessError) {
      ElMessageBox.alert(error.message, '生成失败', {
        confirmButtonText: '知道了',
        type: 'warning',
      })
      return
    }
    attendanceError.value = error instanceof Error ? error.message : '获取加班记录失败'
  } finally {
    attendanceLoading.value = false
  }
}

const formatHours = (value) => `${Number(value || 0).toFixed(1)} 小时`
const formatDate = (value) => {
  if (!value) return '—'
  const date = new Date(`${value}T00:00:00`)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
const getWeekday = (value) => {
  if (!value) return ''
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][
    new Date(`${value}T00:00:00`).getDay()
  ]
}

const createBot = () => {
  dialogVisible.value = false
  ElMessage.success('机器人草稿已创建')
}
</script>

<template>
  <div class="app-shell" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">N</div>
        <span class="brand-name">Nova Bot</span>
      </div>

      <nav class="main-nav">
        <button
          v-for="item in navItems"
          :key="item.id"
          class="nav-item"
          :class="{ active: activeMenu === item.id }"
          :title="collapsed ? item.label : ''"
          @click="activeMenu = item.id"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-bottom">
        <button class="nav-item"><el-icon :size="20"><Setting /></el-icon><span>系统设置</span></button>
        <div class="help-card">
          <div class="help-icon">?</div>
          <strong>需要帮助？</strong>
          <p>查看产品使用指南</p>
          <el-button size="small">访问帮助中心</el-button>
        </div>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <button class="icon-button collapse-button" aria-label="收起侧边栏" @click="collapsed = !collapsed">
          <el-icon :size="19"><Fold /></el-icon>
        </button>
        <div class="topbar-actions">
          <div class="global-search">
            <el-icon><Search /></el-icon>
            <input v-model="search" placeholder="搜索机器人..." />
            <kbd>⌘ K</kbd>
          </div>
          <button class="icon-button notification" aria-label="通知"><el-icon :size="20"><Bell /></el-icon><i /></button>
          <div class="profile">
            <el-avatar :size="38">B</el-avatar>
            <div><strong>bd</strong><span>超级管理员</span></div>
          </div>
        </div>
      </header>

      <div v-if="activeMenu === 'overtime'" class="content attendance-page">
        <section class="attendance-heading">
          <div>
            <div class="heading-icon"><el-icon :size="24"><Clock /></el-icon></div>
            <div>
              <p class="eyebrow">WORK ATTENDANCE</p>
              <h1>加班打卡记录</h1>
              <p>导入考勤文件，即时生成员工月度加班统计与打卡明细</p>
            </div>
          </div>
          <div class="heading-actions">
            <span v-if="lastUpdated" class="updated-time">生成于 {{ lastUpdated }}</span>
          </div>
        </section>

        <input
          ref="fileInput"
          class="hidden-file-input"
          type="file"
          accept=".xlsx,.xls,.csv"
          @change="handleFileChange"
        />

        <section
          class="upload-panel"
          :class="{ dragging: dragActive, selected: selectedFile }"
          @dragenter.prevent="dragActive = true"
          @dragover.prevent="dragActive = true"
          @dragleave.prevent="dragActive = false"
          @drop.prevent="handleDrop"
        >
          <template v-if="!selectedFile">
            <span class="upload-icon"><el-icon :size="30"><UploadFilled /></el-icon></span>
            <div>
              <strong>将考勤文件拖到这里，或点击选择文件</strong>
              <p>支持 .xlsx、.xls、.csv 格式，文件大小不超过 20MB</p>
            </div>
            <el-button type="primary" @click="fileInput?.click()">选择考勤文件</el-button>
          </template>
          <template v-else>
            <span class="file-type">{{ selectedFile.name.split('.').pop()?.toUpperCase() }}</span>
            <div class="selected-file-info">
              <strong>{{ selectedFile.name }}</strong>
              <p>{{ (selectedFile.size / 1024).toFixed(1) }} KB · 文件已就绪</p>
            </div>
            <div class="file-actions">
              <el-button text @click="removeFile">移除</el-button>
              <el-button :icon="Refresh" @click="fileInput?.click()">更换文件</el-button>
              <el-button type="primary" :loading="attendanceLoading" @click="generateAttendance">
                {{ attendanceLoading ? '正在生成' : '生成加班结果' }}
              </el-button>
            </div>
          </template>
        </section>

        <section v-if="hasGenerated" class="attendance-toolbar">
          <div class="toolbar-field">
            <span>统计月份</span>
            <el-select v-model="selectedMonth" placeholder="全部月份" clearable>
              <el-option v-for="month in monthOptions" :key="month" :label="month" :value="month" />
            </el-select>
          </div>
          <div class="employee-search">
            <el-icon><Search /></el-icon>
            <input v-model="employeeKeyword" placeholder="搜索员工姓名" />
          </div>
          <span class="api-status"><i />文件处理完成</span>
        </section>

        <div v-if="attendanceError" class="request-error">
          <el-icon :size="22"><Warning /></el-icon>
          <div>
            <strong>暂时无法生成加班记录</strong>
            <p>{{ attendanceError }}。请确认服务已启动、上传字段为 file，并允许当前页面跨域访问该接口。</p>
          </div>
          <el-button type="primary" @click="generateAttendance">重新生成</el-button>
        </div>

        <template v-else-if="hasGenerated">
          <section class="attendance-stats" v-loading="attendanceLoading">
            <article>
              <span class="attendance-stat-icon purple"><el-icon><User /></el-icon></span>
              <div><strong>{{ attendanceStats.employees }}</strong><span>统计员工</span></div>
            </article>
            <article>
              <span class="attendance-stat-icon blue"><el-icon><Document /></el-icon></span>
              <div><strong>{{ attendanceStats.records }}</strong><span>打卡记录</span></div>
            </article>
            <article>
              <span class="attendance-stat-icon orange"><el-icon><Clock /></el-icon></span>
              <div><strong>{{ attendanceStats.hours.toFixed(1) }}</strong><span>累计加班小时</span></div>
            </article>
            <article>
              <span class="attendance-stat-icon green"><el-icon><Calendar /></el-icon></span>
              <div><strong>{{ attendanceStats.overtimeDays }}</strong><span>产生加班天数</span></div>
            </article>
          </section>

          <section class="panel attendance-list" v-loading="attendanceLoading">
            <div class="panel-head attendance-list-head">
              <div>
                <h2>员工月度明细</h2>
                <p>共 {{ visibleSummaries.length }} 位员工，点击人员行查看每日记录</p>
              </div>
              <div class="attendance-list-actions">
                <span class="success-count">成功处理 {{ attendanceData.successCount }} 条</span>
                <el-button
                  type="primary"
                  size="small"
                  :icon="CopyDocument"
                  :disabled="!batchOvertimeRows.length"
                  @click="copyBatchOvertime"
                >
                  点击复制到加班填写报表
                </el-button>
              </div>
            </div>

            <el-collapse v-if="visibleSummaries.length" v-model="expandedEmployees" class="employee-collapse">
              <el-collapse-item
                v-for="summary in visibleSummaries"
                :key="`${summary.name}-${summary.month}`"
                :name="`${summary.name}-${summary.month}`"
              >
                <template #title>
                  <div class="employee-summary">
                    <div class="employee-identity">
                      <span class="employee-avatar">{{ String(summary.name || '?').slice(0, 1) }}</span>
                      <div><strong>{{ summary.name || '未知员工' }}</strong><span>{{ summary.month }}</span></div>
                    </div>
                    <div class="employee-metrics">
                      <div><span>打卡天数</span><strong>{{ summary.details?.length || 0 }} 天</strong></div>
                      <div><span>加班时长</span><strong class="overtime-total">{{ formatHours(summary.totalOvertimeHours) }}</strong></div>
                    </div>
                  </div>
                </template>

                <div class="attendance-table-wrap">
                  <table class="attendance-table">
                    <thead>
                      <tr>
                        <th>日期</th>
                        <th>日期类型</th>
                        <th>首次打卡</th>
                        <th>末次打卡</th>
                        <th>加班分钟</th>
                        <th>加班时长</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="detail in summary.details || []" :key="detail.attendanceDate">
                        <td>
                          <div class="date-cell"><strong>{{ formatDate(detail.attendanceDate) }}</strong><span>{{ getWeekday(detail.attendanceDate) }}</span></div>
                        </td>
                        <td>
                          <span class="day-type" :class="{ holiday: !detail.workday }">
                            {{ detail.holidayName || (detail.workday ? '工作日' : '休息日') }}
                          </span>
                        </td>
                        <td class="time-cell">{{ detail.firstPunchTime || '—' }}</td>
                        <td class="time-cell">{{ detail.lastPunchTime || '—' }}</td>
                        <td>{{ Number(detail.overtimeMinutes || 0) }} 分钟</td>
                        <td>
                          <span class="hours-badge" :class="{ active: Number(detail.overtimeMinutes) > 0 }">
                            {{ Number(detail.overtimeHours || 0).toFixed(1) }} h
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-empty v-else description="当前筛选条件下没有加班记录" :image-size="90" />
          </section>

          <section v-if="attendanceData.errors.length" class="panel error-list">
            <h2>处理异常</h2>
            <p v-for="(error, index) in attendanceData.errors" :key="index">{{ error }}</p>
          </section>
        </template>

        <section v-else class="import-guide">
          <div class="guide-number">1</div>
          <div><strong>导入考勤文件</strong><p>选择从考勤系统导出的 Excel 或 CSV 文件</p></div>
          <span class="guide-line" />
          <div class="guide-number">2</div>
          <div><strong>后端即时计算</strong><p>文件仅用于本次统计，无需写入数据库</p></div>
          <span class="guide-line" />
          <div class="guide-number">3</div>
          <div><strong>查看生成结果</strong><p>按人员查看月度汇总和每日打卡明细</p></div>
        </section>
      </div>

      <div v-else class="content">
        <section class="page-heading">
          <div>
            <p class="eyebrow">WEDNESDAY, 22 JULY</p>
            <h1>下午好，bd <span>👋</span></h1>
            <p>这是你的智能助手今日运行情况。</p>
          </div>
          <el-button type="primary" :icon="Plus" size="large" @click="dialogVisible = true">创建机器人</el-button>
        </section>

        <section class="stats-grid">
          <article v-for="card in statCards" :key="card.label" class="stat-card">
            <div class="stat-top">
              <span class="stat-icon" :class="card.tone"><el-icon :size="22"><component :is="card.icon" /></el-icon></span>
              <span class="change" :class="{ down: card.change.startsWith('-') }">{{ card.change }}</span>
            </div>
            <strong class="stat-value">{{ card.value }}</strong>
            <span class="stat-label">{{ card.label }}</span>
          </article>
        </section>

        <section class="dashboard-grid">
          <article class="panel chart-panel">
            <div class="panel-head">
              <div><h2>会话趋势</h2><p>过去 12 天的机器人会话量</p></div>
              <el-select model-value="近 12 天" style="width: 116px"><el-option label="近 12 天" value="近 12 天" /></el-select>
            </div>
            <div class="chart-wrap">
              <div class="y-labels"><span>120</span><span>90</span><span>60</span><span>30</span><span>0</span></div>
              <svg viewBox="0 0 600 176" preserveAspectRatio="none" aria-label="会话趋势折线图">
                <defs><linearGradient id="chartFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#6259e8" stop-opacity=".24"/><stop offset="1" stop-color="#6259e8" stop-opacity="0"/></linearGradient></defs>
                <line v-for="y in [0,44,88,132,176]" :key="y" x1="0" :y1="y" x2="600" :y2="y" class="grid-line" />
                <path :d="areaPath.area" fill="url(#chartFill)" />
                <path :d="areaPath.line" class="trend-line" />
                <circle v-for="([x,y], i) in areaPath.points" :key="i" :cx="x" :cy="y" r="3.5" />
              </svg>
            </div>
            <div class="x-labels"><span>7/11</span><span>7/13</span><span>7/15</span><span>7/17</span><span>7/19</span><span>今天</span></div>
          </article>

          <article class="panel source-panel">
            <div class="panel-head"><div><h2>问题来源</h2><p>各渠道会话占比</p></div><button class="icon-button"><el-icon><MoreFilled /></el-icon></button></div>
            <div class="donut-wrap"><div class="donut"><div><strong>2,846</strong><span>总会话</span></div></div></div>
            <div class="legend">
              <div><span class="dot purple" />网页端 <strong>46%</strong></div>
              <div><span class="dot blue" />微信公众号 <strong>28%</strong></div>
              <div><span class="dot cyan" />企业微信 <strong>17%</strong></div>
              <div><span class="dot gray" />其他渠道 <strong>9%</strong></div>
            </div>
          </article>
        </section>

        <section class="panel bot-panel">
          <div class="panel-head">
            <div><h2>机器人概览</h2><p>查看所有机器人当前状态与服务表现</p></div>
            <el-button text type="primary">查看全部 →</el-button>
          </div>
          <div class="table-scroll">
            <table>
              <thead><tr><th>机器人</th><th>状态</th><th>今日会话</th><th>解决率</th><th>最近更新</th><th /></tr></thead>
              <tbody>
                <tr v-for="(bot, index) in filteredBots" :key="bot.name">
                  <td><div class="bot-name"><span :class="`bot-avatar tone-${index}`"><el-icon><DataAnalysis /></el-icon></span><div><strong>{{ bot.name }}</strong><span>{{ bot.desc }}</span></div></div></td>
                  <td><span class="status" :class="{ running: bot.status === '运行中', training: bot.status === '训练中' }">{{ bot.status }}</span></td>
                  <td><strong>{{ bot.sessions.toLocaleString() }}</strong></td>
                  <td><div class="rate"><el-progress :percentage="bot.rate" :stroke-width="6" :show-text="false" /><span>{{ bot.rate }}%</span></div></td>
                  <td class="muted">{{ bot.updated }}</td>
                  <td><button class="icon-button"><el-icon><MoreFilled /></el-icon></button></td>
                </tr>
              </tbody>
            </table>
            <el-empty v-if="!filteredBots.length" description="没有匹配的机器人" :image-size="80" />
          </div>
        </section>
      </div>
    </main>

    <el-dialog v-model="dialogVisible" title="创建新机器人" width="460px">
      <el-form label-position="top">
        <el-form-item label="机器人名称"><el-input placeholder="例如：产品咨询助手" /></el-form-item>
        <el-form-item label="主要用途"><el-input type="textarea" :rows="3" placeholder="简单描述它将处理的问题" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="createBot">创建草稿</el-button></template>
    </el-dialog>

  </div>
</template>
