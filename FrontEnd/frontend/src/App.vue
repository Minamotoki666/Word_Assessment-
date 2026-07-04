<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2> 实时英文单词考试系统 (Java + Vue3 PCG/ECharts)</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 页面一：发起考试 (教师控制台) -->
        <el-tab-pane label=" 发起新考试 (教师端)" name="teacher">
          <TeacherView />
        </el-tab-pane>

        <!-- 页面二：参加考试与在线答题 (学生端) -->
        <el-tab-pane label=" 参加考试 (学生端)" name="student">
          <StudentView />
        </el-tab-pane>

        <!-- 页面三：数据统计与字符云 -->
        <el-tab-pane label=" 字符云与统计报表" name="dashboard">
          <DashboardView />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
// 创建的三个子组件
import TeacherView from './components/TeacherView.vue'
import StudentView from './components/StudentView.vue'
import DashboardView from './components/DashboardView.vue'

const activeTab = ref('teacher')
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}
.card-header h2 {
  margin: 0;
  color: #303133;
  text-align: center;
}
</style>