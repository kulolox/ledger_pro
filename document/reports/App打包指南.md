# App 打包指南

## 打包方式概览

uni-app 打包 App 有两种方式：

| 方式 | 说明 | 适用场景 |
|------|------|----------|
| **HBuilder X 云打包** | 在 HBuilder X 中提交云端编译，无需本地配置 Android SDK / Xcode | 日常开发、快速出包 |
| **本地离线打包** | 用 Android Studio / Xcode 原生工程 + uni-app SDK 编译 | 需要深度定制原生功能、集成第三方 SDK |

推荐优先使用 **HBuilder X 云打包**。

---

## 前置准备

### 1. 安装 HBuilder X

下载 [HBuilder X](https://www.dcloud.io/hbuilderx.html)，选择 **App 开发版**（非 Alpha 版）。

### 2. 注册 DCloud 账号

云打包需要登录 DCloud 开发者账号。在 HBuilder X 右上角登录。

### 3. 项目导入

当前项目使用 Vite + uni-app CLI 构建，目录结构为标准 uni-app 工程，可直接导入：

1. 打开 HBuilder X → 文件 → 导入 → 从本地目录导入
2. 选择 `front_end/` 目录

---

## manifest.json 配置

导入后右键项目 → **manifest.json**，在可视化界面中配置以下项：

### 基础配置

- **应用名称**: LedgerPro
- **版本号**: `versionName` 和 `versionCode`（Android 用纯数字递增）
- **应用图标**: 1024×1024 px 的 PNG 图片

### App 启动图 (Splash)

在 `app-plus` → `splashscreen` 中配置（当前已有基础配置）：

- `alwaysShowBeforeRender: true` — 启动应用时始终显示
- `autoclose: true` — 首页渲染完成后自动关闭
- 上传启动图片（Android 和 iOS 各尺寸）

### App 图标

在可视化配置中上传各尺寸图标，或使用自动生成功能。

### 模块权限

当前项目依赖的模块权限已在 `manifest.json` 中声明：

```json
"modules": {
  "SQLite": {}
}
```

Android 权限已配置网络、相机、存储等基础权限，按需增删。如果不需要相机功能，可移除 `CAMERA`、`FLASHLIGHT` 等相关权限。

### 网络配置

如果后端接口使用 HTTP（非 HTTPS），需在 manifest 中配置：

```json
"app-plus": {
  "distribute": {
    "android": {
      "network": {
        "cleartextTraffic": true
      }
    }
  }
}
```

---

## Android 打包

### 1. 生成证书

在 HBuilder X 中：
1. 菜单 → 发行 → 原生 App 云端打包
2. 在证书管理页面，点击"创建新证书"
3. 填写证书信息：
   - **别名 (Alias)**: 自定义，如 `ledgerpro`
   - **密码**: 牢记，后续应用更新签名必须一致
   - **组织/姓名**: 填写有效信息
4. 保存 `.keystore` 文件到安全位置，**签名证书丢失将无法更新应用**

### 2. 云打包

1. 菜单 → 发行 → 原生 App 云端打包
2. 选择 Android 平台
3. 使用刚才创建的证书
4. 点击"打包"，等待云端编译完成
5. 下载 APK/AAB 产物

### 3. 产物类型

- **APK**: 传统安装包，可直接安装测试
- **AAB** (Android App Bundle): 提交 Google Play 使用

---

## iOS 打包

### 1. 前置条件

- Apple Developer 账号（$99/年）
- macOS + Xcode（申请证书需要）

### 2. 获取证书

在 Apple Developer 后台创建：
1. **App ID** — 确认 Bundle Identifier
2. **开发证书** (.p12) — 用于开发测试
3. **发布证书** (.p12) — 用于 App Store / TestFlight
4. **描述文件** (.mobileprovision) — 绑定证书和设备

DCloud 开发者中心也提供证书协助服务（需付费）。

### 3. 云打包

1. 菜单 → 发行 → 原生 App 云端打包
2. 选择 iOS 平台
3. 上传 `.p12` 证书和 `.mobileprovision` 描述文件
4. 点击"打包"
5. 下载 IPA 文件
6. 用 Xcode → Window → Devices and Simulators 安装到设备，或用 TestFlight 分发

### 4. 免费账号（个人测试）

如果没有付费 Apple Developer 账号：
- 可使用免费 Apple ID 生成开发证书
- 有效期 7 天
- 对接 HBuilder X 基座调试（USB 连接真机运行即可）

---

## 离线打包（简要）

如需深度定制原生层，可使用离线打包：

### Android 离线打包

1. 下载 [uni-app Android 离线 SDK](https://nativesupport.dcloud.net.cn/AppDocs/download/android)
2. 将 SDK 工程导入 Android Studio
3. 将 uni-app 编译产物（`uni build -p app` 生成的 `dist/build/app-plus/`）放入 `assets/apps/` 目录
4. 修改 `dcloud_control.xml` 中的 appid
5. 编译运行

### iOS 离线打包

1. 下载 [uni-app iOS 离线 SDK](https://nativesupport.dcloud.net.cn/AppDocs/download/ios)
2. 用 Xcode 打开 SDK 工程
3. 将编译产物放入指定目录
4. 配置签名和 Bundle Identifier
5. 编译运行

---

## 本地测试与调试

### 真机调试

1. USB 连接手机，开启 USB 调试模式
2. HBuilder X 菜单 → 运行 → 运行到手机
3. 选择设备，等待编译和安装
4. 首次运行会安装 HBuilder 基座
5. 修改代码后可直接热更新

### 自定义基座

如果使用了原生插件或自定义权限配置，需要打自定义基座：

1. 菜单 → 运行 → 运行到手机 → 制作自定义调试基座
2. 等待云端或本地编译
3. 之后可基于自定义基座进行真机调试

---

## 项目特殊配置提醒

### SQLite

LedgerPro 使用 `sql.js` 做本地 SQLite，而非 uni-app 原生 SQLite 模块。`manifest.json` 中声明 `SQLite: {}` 仅保留空配置，实际操作用 `sql.js` WASM 实现。打包 App 时无需额外配置原生 SQLite 插件。

### 网络请求

`src/utils/request.ts` 中 API 地址需要指向服务器实际地址。打包前确认：
- 后端服务已部署到公网或局域网可访问
- `request.ts` 中的 `baseURL` 指向正确地址
- 生产环境建议使用 HTTPS

### 状态栏适配

```json
"globalStyle": {
  "navigationStyle": "custom"
}
```

所有页面使用了自定义导航栏，打包 App 时需注意：
- 状态栏高度在 iOS 和不同 Android 机型上有差异
- 导航栏组件需使用 `uni.getSystemInfoSync().statusBarHeight` 动态适配
- 启动页设置 `"waiting": true` 确保首页渲染完成后再显示

---

## 常见问题

### 1. 打包后白屏/空白页面

- 检查 manifest.json 中路由配置是否正确
- 检查 `pages.json` 首页路径是否存在
- 查看 uni-app 编译日志是否有错误

### 2. 网络请求失败

- Android 9+ 默认禁止 HTTP 明文传输，需在 manifest 中配置 `cleartextTraffic: true`
- 检查 `request.ts` 中 baseURL 是否为服务器实际 IP/域名
- iOS 同样需要 ATS 配置允许 HTTP

### 3. 图标和启动图不生效

- 确保图片尺寸符合要求（图标 1024×1024）
- PNG 格式、无透明通道
- 清理缓存重新打包

### 4. 应用闪退

- 检查是否有未处理的 Promise rejection
- 查看 logcat (Android) 或设备日志 (iOS) 定位崩溃栈

### 5. 跨域问题（App 环境）

- App 中不存在浏览器跨域限制
- 但如果使用 WebView 模式，需在后端配置 CORS 或前端做转发

---

## 参考链接

- [uni-app 离线打包文档](https://nativesupport.dcloud.net.cn/AppDocs/README)
- [HBuilder X 下载](https://www.dcloud.io/hbuilderx.html)
- [Android 证书生成](https://nativesupport.dcloud.net.cn/AppDocs/package/android)
- [iOS 证书申请](https://nativesupport.dcloud.net.cn/AppDocs/package/ios)
- [manifest.json 完整配置](https://uniapp.dcloud.net.cn/collocation/manifest.html)
