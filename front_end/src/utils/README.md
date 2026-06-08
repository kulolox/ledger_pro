# API请求工具使用说明

## 功能特性

- ✅ 支持GET、POST、PUT、DELETE、PATCH等HTTP方法
- ✅ 全局请求/响应/错误拦截器
- ✅ 自动添加Token
- ✅ 自动显示/隐藏Loading
- ✅ 统一错误处理
- ✅ TypeScript类型支持
- ✅ 基于uni.request实现，兼容所有uniapp平台

## 基础使用

### 1. 配置环境变量

在项目根目录创建`.env`文件：

```env
VITE_API_BASE_URL=https://api.example.com
```

### 2. 导入使用

```typescript
import request from '@/utils/request'

// GET请求
const data = await request.get('/api/user/info')

// POST请求
const result = await request.post('/api/user/login', {
  username: 'admin',
  password: '123456'
})

// PUT请求
await request.put('/api/user/info', { nickname: '新昵称' })

// DELETE请求
await request.delete('/api/user/delete/123')
```

## 高级用法

### 自定义配置

```typescript
// 不显示loading
const data = await request.get('/api/data', {}, { showLoading: false })

// 自定义loading文字
const data = await request.get('/api/data', {}, { loadingText: '正在加载...' })

// 自定义超时时间
const data = await request.get('/api/data', {}, { timeout: 60000 })

// 自定义请求头
const data = await request.get('/api/data', {}, {
  header: {
    'Custom-Header': 'value'
  }
})
```

### 添加拦截器

```typescript
import request from '@/utils/request'

// 添加请求拦截器
request.addRequestInterceptor((config) => {
  console.log('请求配置:', config)
  return config
})

// 添加响应拦截器
request.addResponseInterceptor((response) => {
  console.log('响应数据:', response)
  return response
})

// 添加错误拦截器
request.addErrorInterceptor((error) => {
  console.error('错误信息:', error)
  return Promise.reject(error)
})
```

### 动态设置配置

```typescript
import request from '@/utils/request'

// 设置基础URL
request.setBaseURL('https://new-api.example.com')

// 设置超时时间
request.setTimeout(60000)
```

## API接口封装示例

参考`src/api/example.ts`文件，推荐按模块封装API接口：

```typescript
import request from '@/utils/request'

export const userApi = {
  login: (data: { username: string; password: string }) => {
    return request.post<{ token: string }>('/api/user/login', data)
  },

  getUserInfo: () => {
    return request.get<UserInfo>('/api/user/info')
  }
}

// 使用
const { token } = await userApi.login({ username: 'admin', password: '123456' })
const userInfo = await userApi.getUserInfo()
```

## 错误处理

工具已内置全局错误处理，包括：

- HTTP错误（404、500等）
- 业务错误（code !== 0/200）
- 网络错误
- Token过期自动跳转登录

错误会自动显示Toast提示，无需手动处理。

如需自定义错误处理，可以添加错误拦截器：

```typescript
request.addErrorInterceptor((error) => {
  // 自定义错误处理逻辑
  if (error.code === 401) {
    // 特殊处理
  }
  return Promise.reject(error)
})
```

## 类型定义

```typescript
// 请求配置
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: Record<string, any>
  header?: Record<string, string>
  timeout?: number
  showLoading?: boolean
  loadingText?: string
}

// 响应数据
interface ResponseData<T = any> {
  code: number
  data: T
  message: string
}
```

## 注意事项

1. Token默认从`uni.getStorageSync('token')`获取，请确保登录后正确存储
2. 后端接口返回格式应为：`{ code: 0, data: any, message: string }`
3. GET请求会自动添加时间戳参数防止缓存
4. 默认超时时间为30秒
5. 所有请求默认显示loading，可通过`showLoading: false`关闭
