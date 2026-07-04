import request from '../utils/request'

// 1. 教师：创建新考试
export function createExam(params) {
    return request({
        url: '/create',
        method: 'post',
        params: params
    })
}

// 2. 学生：拉取随机打乱题序的考卷
export function getExamPaper(examId) {
    return request({
        url: '/paper',
        method: 'get',
        params: { examId }
    })
}

// 3. 学生：交卷并获取得分
export function submitExam(data) {
    return request({
        url: '/submit',
        method: 'post',
        data: data
    })
}

// 4. 获取指定学生的每周错题字符云
export function getWordCloud(studentId) {
    return request({
        url: '/wordcloud',
        method: 'get',
        params: { studentId }
    })
}

// 5. 教师：一键下载 Excel 成绩统计报表
export function exportExamRecords(examId) {
    return request({
        url: '/export',
        method: 'get',
        params: { examId },
        responseType: 'blob' // 必须配置为 blob 以处理二进制流文件下载
    })
}