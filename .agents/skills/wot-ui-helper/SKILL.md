---
name: wot-ui-helper
description: 当在 LedgerPro 前端编写或修改 UI 组件、需要查询 Wot UI (@wot-ui/ui) 的组件属性、事件、插槽或最佳用法时使用此技能。
---

# Wot UI 组件开发指南与知识库

本项目前端严格采用 **Wot UI (`@wot-ui/ui`)** 组件库，所有组件前缀为 `wd-`（例如 `<wd-button>`, `<wd-toast>`, `<wd-popup>`, `<wd-calendar>` 等）。

## 核心原则

1. **优先组件化**：严禁自行手写重复轮子（如按钮、弹出层、下拉选择、气泡提示、步进器等），必须优先使用 Wot UI 组件。
2. **渐进式知识库查阅**：
   * 详细的 Wot UI 常见组件 API、属性与范例定义请查阅：
     [wotui-knowledge.md](../../../document/reference/wotui-knowledge.md)
   * 结构化组件属性数据请查阅：
     [wotui-data.json](../../../document/reference/wotui-data.json)

## 常用开发模式

### 1. Toast 与 Message 轻提示
在 uni-app 中使用 Wot UI 提示时，页面中必须引入对应的组件并挂载：
```vue
<template>
  <view class="container">
    <wd-toast />
    <wd-message-box />
  </view>
</template>

<script setup lang="ts">
import { useToast, useMessage } from 'wot-design-uni'

const toast = useToast()
const message = useMessage()

// 成功提示
toast.success('操作成功')

// 异常或校验提示
toast.error('金额输入有误')
</script>
```

### 2. 表单与弹窗规范
* 表单校验使用 `wd-form` 配合 Jakarta 校验对应的规则。
* 弹出选择框（如分类选择、时间选择）统一使用 `wd-picker` 或 `wd-datetime-picker`，配合 `wd-popup` 实现底部滑出交互。
