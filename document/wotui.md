基于记账App项目（uni-app前端），整理所采用UI组件库 **Wot Design Uni** 的核心组件用法技术文档，确保开发规范统一。

---

## Wot Design Uni 组件用法技术文档

### 1. 概述

**Wot Design Uni** 是一个基于 Vue3 和 uni-app 的高质量移动端组件库，提供丰富的预设组件，可用于 iOS/Android/H5/小程序多端。本技术文档聚焦一期记账功能所需核心组件，覆盖导航、表单、反馈、展示等类别，所有用法均遵循官方最佳实践。

**版本**：`^1.0.0`（以实际安装为准）  
**官方文档**：[https://wot-design-uni.cn/](https://wot-design-uni.cn/)

### 2. 安装与引入

**安装**：

```bash
npm install wot-design-uni
```

**全局配置（`main.js`）**：

```javascript
import WotDesignUni from "wot-design-uni";
import "wot-design-uni/dist/wot-design-uni.css";

Vue.use(WotDesignUni);
```

**按需引入**（推荐，减少打包体积）：

- 配合 `easycom` 自动引入（`pages.json` 中配置）：

```json
{
  "easycom": {
    "autoscan": true,
    "custom": {
      "^wd-(.*)": "wot-design-uni/components/wd-$1/wd-$1.vue"
    }
  }
}
```

### 3. 一期核心组件用法

#### 3.1 布局与导航

**wd-navbar** – 自定义导航栏（在记账页顶部使用）

```html
<wd-navbar title="记账" left-text="返回" @click-left="goBack" />
```

- 支持左右插槽，可放置图标、账本名等。

**wd-tabs** – 分类切换（支出/收入/转账）

```html
<wd-tabs v-model="activeTab">
  <wd-tab-pane title="支出" name="expense" />
  <wd-tab-pane title="收入" name="income" />
  <wd-tab-pane title="转账" name="transfer" />
</wd-tabs>
```

#### 3.2 金额输入与键盘

**特殊说明**：Wot Design 没有直接提供数字计算键盘，因此采用 **wd-input** 配合自定义键盘组件。数字键盘需自行封装，但可复用 `wd-number-keyboard`（如果有）或使用 `wd-keyboard` 实现。

**wd-input** – 金额显示框（只读）

```html
<wd-input
  v-model="amount"
  readonly
  placeholder="0.00"
  align="center"
  custom-style="font-size: 40rpx;"
/>
```

**自定义数字键盘**（基于 `wd-grid` + `wd-button` 实现，或采用第三方）

> 若使用 `wot-design-uni` 内置的 `wd-number-keyboard`：

```html
<wd-number-keyboard
  v-model="showKeyboard"
  :extra-key="'.'"
  @input="onInput"
  @delete="onDelete"
  @confirm="onConfirm"
/>
```

- `@input` 返回按键值，可拼接计算表达式。

**计算器逻辑**：在 `onInput` 中处理四则运算符号（+、-、×、÷），先构建表达式，点击“保存”时使用 `eval` 或自行解析计算最终金额（注意安全，仅数字和运算符）。

#### 3.3 分类选择

**wd-icon** – 分类图标

```html
<wd-icon name="food" size="40rpx" />
```

- 图标库名基于 `wot-design-uni` 内置图标，也可自定义。

**水平滚动分类列表**  
使用 `wd-scroll-view` 实现横向滑动：

```html
<wd-scroll-view scroll-x>
  <view
    v-for="cat in categories"
    :key="cat.id"
    class="cat-item"
    @click="selectCat(cat)"
  >
    <wd-icon :name="cat.icon" size="30rpx" />
    <text>{{ cat.name }}</text>
  </view>
</wd-scroll-view>
```

**选中态**：通过动态 class 绑定高亮样式。

#### 3.4 交易列表（主界面下拉）

**下拉刷新与加载**  
使用 `wd-loadmore` 和 `wd-refresher`（或页面 `onPullDownRefresh`），配合 `scroll-view`。

```html
<wd-loadmore v-if="loading" state="loading" />
<wd-loadmore v-if="finished" state="finished" text="没有更多了" />
```

**交易条目**  
使用 `wd-cell` 展示：

```html
<wd-cell :title="catName" :label="time" :value="'¥' + amount">
  <template #icon>
    <wd-icon :name="catIcon" size="32rpx" />
  </template>
</wd-cell>
```

- 左滑删除可使用 `wd-swipe-action`。

#### 3.5 快捷操作与弹窗

**wd-popup** – 记账备注/附件浮层

```html
<wd-popup v-model="showRemark" position="bottom" :safe-area-inset-bottom="true">
  <wd-input v-model="remark" placeholder="添加备注" clearable />
  <wd-button @click="addPhoto">拍照</wd-button>
  <wd-button @click="saveRemark">完成</wd-button>
</wd-popup>
```

**wd-action-sheet** – 选择图片来源

```html
<wd-action-sheet
  v-model="showSheet"
  :actions="actions"
  @select="handleSelect"
/>
```

**wd-message-box** – 确认/提示

```html
<wd-message-box title="确定删除？" show-cancel-button @confirm="doDelete" />
```

#### 3.6 登录/注册相关

**wd-form** / **wd-input** – 表单

```html
<wd-form ref="form" :model="loginForm">
  <wd-input v-model="loginForm.email" placeholder="邮箱" clearable />
  <wd-input
    v-model="loginForm.password"
    placeholder="密码"
    type="password"
    clearable
  />
  <wd-button type="primary" block @click="login">登录</wd-button>
</wd-form>
```

**wd-button** – 按钮

```html
<wd-button type="primary" size="large" :loading="loading">登录</wd-button>
```

**wd-toast** – 轻提示

```javascript
this.$toast.success("保存成功");
```

### 4. 主题定制

在 `App.vue` 中引入 CSS 变量覆盖默认主题色，保持记账 App 极简风格：

```css
:root {
  --wot-primary: #07c160; /* 主题绿 */
  --wot-warning: #ff6b6b;
  --wot-radius: 12rpx;
}
```

- 参考官方文档的 CSS 变量表。

### 5. 注意事项

- **跨端兼容**：`wd-number-keyboard` 在 H5 和小程序表现可能有差异，务必在目标平台测试。若不可用，建议封装纯自定义键盘（无依赖）。
- **性能**：交易列表长列表使用虚拟列表（`wd-recycle-scroller` 或 uni-app 的 `recycle-view`）。
- **图标**：Wot Design 图标有限，可引入 `iconfont` 扩展分类图标。
- **表单校验**：可使用 `async-validator` 与 `wd-form` 配合。
