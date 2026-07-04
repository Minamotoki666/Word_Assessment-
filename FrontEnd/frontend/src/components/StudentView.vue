<template>
  <div class="student-view">
    <!-- 阶段一：进入考场前的身份确认 -->
    <el-card v-if="step === 'login'" class="box-card login-box">
      <template #header>
        <div class="header-title"> 考生信息与进场身份登记</div>
      </template>
      <el-form :model="loginForm" label-width="90px">
        <el-form-item label="考卷 ID">
          <el-input v-model.number="loginForm.examId" placeholder="请输入老师分配的考试ID数字" />
        </el-form-item>
        <el-form-item label="学 号">
          <el-input v-model="loginForm.studentId" placeholder="例如：20260401" />
        </el-form-item>
        <el-form-item label="姓 名">
          <el-input v-model="loginForm.studentName" placeholder="例如：张三" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleEnterExam" style="width: 100%;">
             提取试卷并开始考试
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 阶段二：在线考试答题界面 -->
    <div v-else-if="step === 'exam'" class="exam-paper">
      <!-- 试卷头部信息悬浮与倒计时 -->
      <div class="paper-header">
        <div>
          <h3 style="margin: 0;">{{ paperData.title }}</h3>
          <span class="user-badge">考生：{{ loginForm.studentName }} ({{ loginForm.studentId }})</span>
        </div>
        <div class="timer-box">
           剩余时间: <span class="countdown">{{ countdownText }}</span>
        </div>
      </div>

      <!-- 乱序生成的选择题列表 -->
      <div class="question-list">
        <el-card
            v-for="(item, index) in paperData.questions"
            :key="item.wordId"
            class="question-card mb-20"
        >
          <div class="question-title">
            <span class="q-num">{{ index + 1 }}.</span>
            英文单词：<span class="en-word">{{ item.wordEn }}</span>
          </div>

          <el-radio-group v-model="userAnswers[item.wordId]" class="option-group">
            <el-radio
                v-for="(opt, optIdx) in item.options"
                :key="optIdx"
                :label="opt"
                border
                class="option-item"
            >
              {{ String.fromCharCode(65 + optIdx) }}. {{ opt }}
            </el-radio>
          </el-radio-group>
        </el-card>
      </div>

      <!-- 底部交卷控制栏 -->
      <div class="submit-bar">
        <el-popconfirm
            title="交卷后无法更改答案，确定要立即提交吗？"
            confirm-button-text="确认交卷"
            cancel-button-text="再检查下"
            @confirm="handleSubmitExam(false)"
        >
          <template #reference>
            <el-button type="success" size="large" :loading="submitting">
               完成答题，立即交卷
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <!-- 阶段三：考试结束成绩反馈 -->
    <el-result
        v-else-if="step === 'result'"
        icon="success"
        title=" 考试已结束！"
        :sub-title="`感谢你的认真作答，你的客观题得分已生成。记录ID：${resultData.recordId}`"
    >
      <template #extra>
        <div class="score-card">
          <div class="score-text">{{ resultData.score }} <span class="unit">分</span></div>
          <div class="detail-text">共 {{ resultData.total }} 道题</div>
        </div>
        <el-button type="primary" @click="handleBackLogin">返回考试主界面</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getExamPaper, submitExam } from '../api/exam'

const step = ref('login') // 'login' | 'exam' | 'result'
const loading = ref(false)
const submitting = ref(false)

const loginForm = reactive({
  examId: 1,
  studentId: '20260001',
  studentName: '专业英语同学'
})

const paperData = ref({})
const userAnswers = reactive({}) // { wordId: '选择的释义' }
const resultData = ref({})

// 倒计时核心逻辑
const countdownText = ref('00:00:00')
let timer = null

const startTimer = (endTimeStr) => {
  if (timer) clearInterval(timer)

  const updateTimer = () => {
    // 跨浏览器兼容时间格式
    const end = new Date(endTimeStr.replace(/-/g, '/')).getTime()
    const now = new Date().getTime()
    const diff = end - now

    if (diff <= 0) {
      countdownText.value = '已超时'
      clearInterval(timer)
      ElMessage.error('考试时间到！系统正在为您自动强行交卷...')
      handleSubmitExam(true) // 超时强制提交
      return
    }

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    countdownText.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }

  updateTimer()
  timer = setInterval(updateTimer, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 拉取试卷
const handleEnterExam = async () => {
  if (!loginForm.examId || !loginForm.studentId || !loginForm.studentName) {
    return ElMessage.warning('请将进场信息填写完整！')
  }

  loading.value = true
  try {
    const res = await getExamPaper(loginForm.examId)
    paperData.value = res

    // 初始化空答案字典
    res.questions.forEach(q => {
      userAnswers[q.wordId] = ''
    })

    step.value = 'exam'
    startTimer(res.endTime)
    ElMessage.success('成功提取试卷，注意顶部的剩余时间！')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 交卷处理
const handleSubmitExam = async (isTimeout = false) => {
  if (submitting.value) return
  submitting.value = true
  if (timer) clearInterval(timer)

  // 格式化数据给后端 SubmitExamDTO
  const answersList = Object.keys(userAnswers).map(wordIdStr => ({
    wordId: Number(wordIdStr),
    selectedAnswer: userAnswers[wordIdStr]
  }))

  try {
    const res = await submitExam({
      examId: loginForm.examId,
      studentId: loginForm.studentId,
      studentName: loginForm.studentName,
      answers: answersList
    })

    resultData.value = res
    step.value = 'result'
    if (!isTimeout) {
      ElMessage.success('答案批改完毕！')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('交卷失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleBackLogin = () => {
  step.value = 'login'
}
</script>

<style scoped>
.login-box {
  max-width: 480px;
  margin: 40px auto;
}
.header-title {
  font-weight: bold;
  font-size: 16px;
}
.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f5f7fa;
  padding: 15px 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  margin-bottom: 20px;
}
.user-badge {
  font-size: 13px;
  color: #606266;
}
.timer-box {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}
.countdown {
  font-family: monospace;
  font-size: 20px;
}
.question-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
}
.q-num {
  font-weight: bold;
  color: #409eff;
  margin-right: 5px;
}
.en-word {
  font-weight: bold;
  color: #e6a23c;
  font-size: 20px;
}
.option-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
}
.option-item {
  width: 100%;
  margin-right: 0 !important;
  display: flex;
}
.submit-bar {
  text-align: center;
  margin: 30px 0;
}
.score-card {
  background: #f0f9eb;
  padding: 20px 40px;
  border-radius: 8px;
  margin: 20px auto;
  border: 1px solid #e1f3d8;
}
.score-text {
  font-size: 48px;
  color: #67c23a;
  font-weight: bold;
}
.unit {
  font-size: 18px;
}
.detail-text {
  color: #909399;
  font-size: 14px;
}
.mb-20 {
  margin-bottom: 20px;
}
</style>