import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 404) {
        return Promise.reject(new Error('请求的资源不存在'))
      } else if (status === 500) {
        return Promise.reject(new Error('服务器内部错误'))
      } else if (status === 403) {
        return Promise.reject(new Error('没有权限访问'))
      }
    } else if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请稍后重试'))
    } else if (!window.navigator.onLine) {
      return Promise.reject(new Error('网络连接已断开'))
    }
    return Promise.reject(new Error('网络请求失败: ' + (error.message || '未知错误')))
  }
)

export default instance
