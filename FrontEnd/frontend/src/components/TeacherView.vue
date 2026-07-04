<template>
  <div class="teacher-view">
    <el-alert
        title=" 操作指南：在这里设置本次测验的时间范围和抽题量。点击提交后，系统将从现有词库中随机洗牌，生成考卷集。"
        type="info"
        show-icon
        :closable="false"
        class="mb-20"
    />

    <el-form :model="form" label-width="120px" class="exam-form">
      <el-form-item label="考试名称">
        <el-input v-model="form.title" placeholder="例如：计算机专业英语四六级模拟测试一" />
      </el-form-item>

      <el-form-item label="开始时间">
        <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择考试开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="结束时间">
        <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择考试截止时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="随机题量">
        <el-input-number v-model="form.questionCount" :min="1" :max="100" label="题目数量" />
        <span class="tip-text">道单词选择题</span>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleCreateExam">
           立即发起考试
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 成功后的弹窗提示 -->
    <el-dialog v-model="showSuccessDialog" title=" 发起考试成功！" width="450px" center>
      <div class="success-box">
        <p>系统已为所有考生生成统一考题集合！</p>
        <div class="exam-id-box">
          本次考试分配的 ID：<span class="id-text">{{ createdExamId }}</span>
        </div>
        <p class="sub-text">请将上方 ID 告知考生，考生可在“学生端”输入该 ID 参与考试。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showSuccessDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createExam } from '../api/exam'

const loading = ref(false)
const showSuccessDialog = ref(false)
const createdExamId = ref(null)

// 自动生成默认的时间间隔（当前时间开始，往后两小时）
const getNowStr = () => {
  const d = new Date()
  return d.toISOString().replace('T', ' ').substring(0, 19)
}
const getLaterStr = () => {
  const d = new Date()
  d.setHours(d.getHours() + 2)
  return d.toISOString().replace('T', ' ').substring(0, 19)
}

const form = reactive({
  title: 'Java课程设计专业英语阶段测试',
  startTime: getNowStr(),
  endTime: getLaterStr(),
  questionCount: 5 // 默认抽5道题测试
})

const handleCreateExam = async () => {
  if (!form.title || !form.startTime || !form.endTime) {
    return ElMessage.warning('请将基础信息填写完整！')
  }
  if (new Date(form.startTime) >= new Date(form.endTime)) {
    return ElMessage.warning('开始时间必须早于结束时间！')
  }

  loading.value = true
  try {
    const resId = await createExam({
      title: form.title,
      startTime: form.startTime,
      endTime: form.endTime,
      questionCount: form.questionCount
    })
    createdExamId.value = resId
    showSuccessDialog.value = true
    ElMessage.success('考试创建成功！')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.teacher-view {
  padding: 15px;
}
.mb-20 {
  margin-bottom: 20px;
}
.exam-form {
  max-width: 600px;
  margin: 20px auto;
}
.tip-text {
  margin-left: 10px;
  color: #909399;
}
.success-box {
  text-align: center;
  font-size: 15px;
}
.exam-id-box {
  margin: 20px 0;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 6px;
  color: #67c23a;
  font-weight: bold;
}
.id-text {
  font-size: 24px;
}
.sub-text {
  font-size: 13px;
  color: #909399;
}
</style>