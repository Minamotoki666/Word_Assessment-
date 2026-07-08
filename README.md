# Word_Assessment-
实时英文单词考试系统

## 项目简介
本项目为一个支持动态生成试卷、实时判卷反馈、且具备数据可视化分析的在线专业英语单词测试系统。包含题序打乱防作弊、ECharts 错题字符云、EasyExcel 成绩导出等特色功能。

## 技术栈
* **后端：** Java 17 + Spring Boot 3 + MyBatis-Plus + EasyExcel
* **前端：** Vue 3 + Vite + Element-Plus + ECharts
* **数据库：** MySQL 8.0

## 快速启动指南

### 1. 数据库准备
1. 在本地 MySQL 中创建一个名为 `word_exam_system` 的数据库。
2. 运行本项目 `sql` 文件夹下的 `word_exam_system.sql` 脚本，完成表结构初始化和六级词库数据的导入。

### 2. 后端启动 (Backend)
1. 使用 IntelliJ IDEA 打开 `backend` 文件夹。
2. 找到 `src/main/resources/application.yml`，将 `username` 和 `password` 修改为你本机的 MySQL 账号密码。
3. 刷新 Maven 依赖后，运行 `DemoApplication.java` 启动后端服务（默认运行在 8080 端口）。

### 3. 前端启动 (Frontend)
请确保你的电脑已安装 Node.js。
1. 进入前端目录：`cd frontend`
2. 安装项目依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 打开浏览器访问控制台输出的地址（通常为 `http://localhost:5173`）即可体验系统！

## 注意事项
* 教师端创建考试时，请确保数据库内题库数量大于抽题数量。
* 如果前端提示 `Network Error`，请检查后端是否正常启动，以及端口是否被占用。
