import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
    baseURL: 'http://localhost:8080/api/exam', // 后端 API 根路径
    timeout: 10000 // 请求超时设置
})

// 响应拦截器：统一处理后端返回结果和异常捕获
service.interceptors.response.use(
    response => {
        // 如果是下载文件的二进制流，直接放行返回
        if (response.config.responseType === 'blob') {
            return response
        }
        return response.data
    },
    error => {
        console.error('API Error:', error)
        // 自动弹窗提示后端抛出的异常（如：考试尚未开始、题库数量不足等）
        ElMessage.error(error.response?.data?.message || error.message || '网络请求失败，请检查后端是否启动！')
        return Promise.reject(error)
    }
)

export default service