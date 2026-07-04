<template>
  <div class="dashboard-view">
    <el-row :gutter="20">
      <!-- 左侧卡片：动态错题字符云展示 -->
      <el-col :span="14">
        <el-card class="box-card chart-card">
          <template #header>
            <div class="card-header">
              <span> 学生专属错题字符云 (答错高频词分析)</span>
            </div>
          </template>

          <!-- 查询工具栏 -->
          <div class="toolbar mb-20">
            <el-input
                v-model="studentIdQuery"
                placeholder="输入查询学号 (如：20260001)"
                style="width: 220px; margin-right: 10px;"
            />
            <el-button type="primary" :loading="chartLoading" @click="fetchAndRenderWordCloud">
               生成专属错题云
            </el-button>
          </div>

          <!-- ECharts 图表容器 (必须指定确切的高度和宽度) -->
          <div ref="wordCloudRef" class="word-cloud-container"></div>
        </el-card>
      </el-col>

      <!-- 右侧卡片：成绩 Excel 统计报表导出 -->
      <el-col :span="10">
        <el-card class="box-card export-card">
          <template #header>
            <div class="card-header">
              <span> 成绩统计报表一键导出 (Excel)</span>
            </div>
          </template>

          <div class="export-box">
            <el-alert
                title="提示：支持指定考场导出。系统将自动提取考生得分与交卷时间，生成清晰易读的 Excel 文档。"
                type="success"
                :closable="false"
                class="mb-20"
            />

            <el-form label-position="top">
              <el-form-item label="待导出的考试 ID (选填，留空则导出系统全量记录)">
                <el-input v-model="exportExamId" placeholder="例如输入数字 1，留空则全量" />
              </el-form-item>

              <el-form-item>
                <el-button
                    type="success"
                    size="large"
                    icon="Download"
                    :loading="exporting"
                    @click="handleExportExcel"
                    style="width: 100%;"
                >
                   下载 Excel 成绩表 (.xlsx)
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import 'echarts-wordcloud' // 必须引入字符云插件扩展
import { getWordCloud, exportExamRecords } from '../api/exam'

const studentIdQuery = ref('20260001')
const chartLoading = ref(false)
const wordCloudRef = ref(null)
let myChart = null

const exportExamId = ref('')
const exporting = ref(false)

// 初始化并渲染 ECharts 字符云
const renderChart = (dataList) => {
  if (!myChart) {
    myChart = echarts.init(wordCloudRef.value)
  }

  // 字符云专属 ECharts 配置项
  const option = {
    tooltip: {
      show: true,
      formatter: (item) => `${item.name}: 累计答错 <b>${item.value}</b> 次`
    },
    series: [
      {
        type: 'wordCloud',
        shape: 'circle', // 云图形状
        gridSize: 15,    // 单词之间的间隔
        sizeRange: [16, 60], // 字体大小范围 (最小16px, 最大60px)
        rotationRange: [-45, 45], // 旋转角度范围
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: 'bold',
          // 随机生成亮眼的莫兰迪与科技风糖果色
          color: () => {
            const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#9B51E0', '#00BCD4', '#FF5722']
            return colors[Math.floor(Math.random() * colors.length)]
          }
        },
        data: dataList
      }
    ]
  }

  myChart.setOption(option)
}

// 拉取字符云后端数据
const fetchAndRenderWordCloud = async () => {
  if (!studentIdQuery.value) {
    return ElMessage.warning('请输入学号后查询！')
  }

  chartLoading.value = true
  try {
    const res = await getWordCloud(studentIdQuery.value)
    if (!res || res.length === 0) {
      ElMessage.info('当前学号近期暂无答错记录，先去“参加考试”里随意错几道题吧！')
      // 渲染提示假数据以展示图表效果
      renderChart([
        { name: 'No Errors Found', value: 30 },
        { name: 'Prefect Student', value: 25 },
        { name: 'Try More Exams', value: 20 }
      ])
    } else {
      renderChart(res)
      ElMessage.success('专属错题字符云生成完毕！')
    }
  } catch (error) {
    console.error(error)
  } finally {
    chartLoading.value = false
  }
}

// 页面加载完成后默认拉取一次默认学号的数据
onMounted(() => {
  nextTick(() => {
    fetchAndRenderWordCloud()
  })
})

// 处理 Excel 二进制流下载
const handleExportExcel = async () => {
  exporting.value = true
  try {
    const res = await exportExamRecords(exportExamId.value || undefined)

    // 利用浏览器的 Blob 机制原地触发下载
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    // 设定默认导出的文件名
    link.setAttribute('download', `英文单词考试成绩表_${exportExamId.value || '全量'}.xlsx`)
    document.body.appendChild(link)
    link.click()

    // 清理临时 DOM
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('Excel 报表已成功下载至本地！')
  } catch (error) {
    console.error(error)
    ElMessage.error('文件下载失败，请检查考场是否存在记录！')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.dashboard-view {
  padding: 10px 5px;
}
.word-cloud-container {
  width: 100%;
  height: 420px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
}
.mb-20 {
  margin-bottom: 20px;
}
.export-box {
  padding: 10px;
}
</style>