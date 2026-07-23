# Wot UI 组件知识库

> 来源: https://wot-ui.cn/guide/introduction.html
> 版本: 2.2.0 | 组件数: 80+ | 自动采集于 2026-07-23

---

## 基础组件 Basic

### button

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| type | 按钮类型，可选值为 primary、success、info、warning、danger | string | primary |
| variant | 按钮变体，可选值为 base、plain、dashed、soft、subtle、text | string | base |
| size | 按钮尺寸，可选值为 mini、small、medium、large | string | medium |
| round | 圆角按钮 | boolean | false |
| disabled | 禁用按钮 | boolean | false |
| hairline | 细边框 | boolean | false |
| block | 块状按钮 | boolean | false |
| loading | 加载中按钮 | boolean | false |
| text | 按钮文本 | string | - |
| icon | 图标类名 | string | - |
| class-prefix | 类名前缀，用于使用自定义图标，用法参考 Icon 组件 | string | wd-icon |
| css-icon | CSS 图标，用法参考 Icon 组件 | boolean | string | false |
| loading-props | 加载配置项 | Partial<LoadingProps> | - |
| open-type | 开放能力类型，详见下方 ButtonOpenType | string | - |
| hover-stop-propagation | 阻止祖先节点点击态 | boolean | false |
| hover-start-time | 按下后多久出现点击态（ms） | number | 20 |
| hover-stay-time | 松开后多久移除点击态（ms） | number | 70 |
| lang | 用户信息语言，可选值为 zh_CN、zh_TW、en | string | - |
| session-from | 会话来源（open-type=contact 时有效） | string | - |
| send-message-title | 会话消息卡片标题（open-type=contact 时有效） | string | 当前标题 |
| send-message-path | 会话消息卡片路径（open-type=contact 时有效） | string | 当前分享路径 |
| send-message-img | 会话消息卡片图片（open-type=contact 时有效） | string | 截图 |
| app-parameter | 打开 APP 传参（open-type=launchApp 时有效） | string | - |
| show-message-card | 显示会话消息卡片（open-type=contact 时有效） | boolean | false |
| button-id | 按钮唯一标识 | string | - |
| scope | 支付宝授权范围，可选值为 phoneNumber、userInfo（open-type=getAuthorize 时有效） | string | - |
| loading-color | 加载图标颜色 | string | - |
| custom-class | 根节点自定义类名 | string | - |
| custom-style | 根节点自定义样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击事件 | event |
| getuserinfo | 获取用户信息回调 | event |
| contact | 客服消息回调（open-type=contact） | event |
| getphonenumber | 获取手机号回调（open-type=getPhoneNumber） | event |
| getrealtimephonenumber | 实时获取手机号回调（open-type=getRealtimePhoneNumber） | event |
| error | 开放能力错误回调（open-type=launchApp） | event |
| launchapp | 打开 APP 成功回调（open-type=launchApp） | event |
| opensetting | 打开授权设置页回调（open-type=openSetting） | event |
| chooseavatar | 获取用户头像回调（open-type=chooseAvatar） | event |
| agreeprivacyauthorization | 同意隐私协议回调（open-type=agreePrivacyAuthorization） | event |

#### Slots

| name | 说明 |
| --- | --- |
| default | 按钮内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-button-primary-bg | $primary-6 | 主类型背景色 |
| --wot-button-primary-bg-active | $primary-7 | 主类型背景激活色 |
| --wot-button-primary-color | $primary-6 | 主类型文本色 |
| --wot-button-primary-color-active | $primary-7 | 主类型文本激活色 |
| --wot-button-primary-plain-bg | $filled-zero | 主类型 plain 背景色 |
| --wot-button-primary-plain-bg-active | $primary-1 | 主类型 plain 背景激活色 |
| --wot-button-primary-plain-border | $primary-6 | 主类型 plain 边框色 |
| --wot-button-primary-plain-border-active | $primary-7 | 主类型 plain 边框激活色 |
| --wot-button-primary-soft-bg | $primary-1 | 主类型 soft 背景色 |
| --wot-button-primary-soft-bg-active | $primary-2 | 主类型 soft 背景激活色 |
> 完整 81 个 CSS 变量见源码

### icon

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| name | 图标名称或图片链接 | string | - |
| color | 图标的颜色 | string | inherit |
| size | 图标的字体大小 | string | number | inherit |
| class-prefix | 类名前缀，用于使用自定义图标 | string | wd-icon |
| css-icon | CSS 图标，为 true 时 name 直接作为 CSS class 而不会拼接 class-prefix 前缀，也可以直接传图标类名 | boolean | string | false |
| custom-style | 根节点样式 | string | - |
| custom-class | 根节点样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击图标时触发 | event |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-icon-font-size | $typography-body-size-main | 图标字号 |
> 完整 1 个 CSS 变量见源码

### text

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| type | 主题类型，可选值为 default、primary、success、warning、error | TextType | default |
| text | 文本内容 | `string | number` |
| size | 字体大小 | string | '' |
| mode | 文本处理模式，可选值为 text、date、phone、name、price | TextMode | text |
| decoration | 文本装饰，可选值为 none、underline、line-through、overline | TextDecoration | none |
| call | mode="phone" 时点击文本是否拨号 | boolean | false |
| bold | 是否粗体 | boolean | false |
| format | 是否脱敏，仅在 mode 为 phone 和 name 时生效 | boolean | false |
| color | 文本颜色 | string | '' |
| prefix | 前缀内容 | string | - |
| suffix | 后缀内容 | string | - |
| lines | 展示行数，超出后显示省略号 | number | - |
| line-height | 文本行高 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击文本时触发 | Event |

#### Slots

| name | 说明 |
| --- | --- |
| prefix | 前缀内容 |
| suffix | 后缀内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-text-font-size | $typography-body-size-main | 默认文字字号 |
| --wot-text-line-height | $typography-body-line-height-size-main | 默认文字行高 |
| --wot-text-primary-color | $primary-6 | 主色文字颜色 |
| --wot-text-success-color | $success-main | 成功文字颜色 |
| --wot-text-error-color | $danger-main | 错误文字颜色 |
| --wot-text-warning-color | $warning-main | 警告文字颜色 |
| --wot-text-default-color | $text-auxiliary | 默认文字颜色 |
> 完整 7 个 CSS 变量见源码

### layout

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| gutter | 列元素之间的间距（单位为 px） | number | 0 |
| justify | 主轴对齐方式，可选值为 start、center、end、space-between、space-around、space-evenly | string | start |
| align | 交叉轴对齐方式，可选值为 top、middle、bottom | string | top |
| custom-class | 根节点样式类名 | string | - |
| custom-style | 根节点样式 | string | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 行内容 |

### cell

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| title | 分组标题 | string | - |
| value | 分组右侧内容 | string | - |
| border | 是否展示外边框 | boolean | false |
| insert | 是否展示为圆角卡片风格 | boolean | false |
| center | 是否使内容垂直居中 | boolean | false |
| size | 单元格大小，支持 large | string | - |
| title-width | 左侧标题宽度 | string | number | - |
| layout | 单元格布局方式，支持 horizontal / vertical | string | - |
| value-align | 右侧内容对齐方式，支持 left / right / center | string | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 当 clickable 或 is-link 为 true 时，点击单元格触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| title | 分组标题 |
| value | 分组右侧内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-cell-bg | $filled-oppo | 单元格背景色 |
| --wot-cell-bg-active | $filled-bottom | 单元格激活态背景色 |
| --wot-cell-title-color | $text-main | 标题文字颜色 |
| --wot-cell-label-color | $text-auxiliary | 标签文字颜色 |
| --wot-cell-value-color | $text-secondary | 值文字颜色 |
| --wot-cell-icon-color | $icon-secondary | 图标颜色 |
| --wot-cell-theme-color | $primary-6 | 主题色 |
| --wot-cell-arrow-color | $icon-auxiliary | 箭头颜色 |
| --wot-cell-border-color | $border-main | 边框颜色 |
| --wot-cell-clear-color | $icon-disabled | 清除图标颜色 |
> 完整 40 个 CSS 变量见源码

### fab

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model:active | 是否激活 | boolean | false |
| type | 悬浮按钮类型，可选值为 primary、success、info、warning、danger | FabType | 'primary' |
| position | 悬浮按钮位置，可选值为 left-top、right-top、left-bottom、right-bottom、left-center、right-center、top-center、bottom-center | FabPosition | 'right-bottom' |
| draggable | 按钮能否拖动 | boolean | false |
| direction | 悬浮按钮菜单弹出方向，可选值为 top、right、bottom、left | FabDirection | 'top' |
| disabled | 是否禁用 | boolean | false |
| inactive-icon | 悬浮按钮未展开时的图标 | string | 'plus' |
| active-icon | 悬浮按钮展开时的图标 | string | 'close' |
| z-index | 自定义悬浮按钮层级 | number | 99 |
| gap | 自定义悬浮按钮与可视区域边缘的间距 | FabGap | { top: 16, right: 16, bottom: 16, left: 16 } |
| expandable | 用于控制点击时是否展开菜单，设置为 false 时触发 click 事件 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | expandable 设置为 false 时，点击悬浮组件内部但不触发内部逻辑时则抛出版事件 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 动作按钮区域内容 |
| trigger | 触发器插槽，用于完全自定义点击触发锚点区域 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 展开菜单 | - |
| close | 收起菜单 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-fab-trigger-width | $n-60 | 浮动按钮触发器宽度 |
| --wot-fab-trigger-height | $n-60 | 浮动按钮触发器高度 |
| --wot-fab-actions-padding | $padding-extra-loose | 动作列表主轴内边距 |
| --wot-fab-z-index | 99 | 浮动按钮层级 |
| --wot-fab-transition-offset | $n-40 | 动作按钮展开/收起动画偏移距离 |
| --wot-fab-trigger-icon-size | $n-32 | 浮动按钮触发器图标大小 |
> 完整 6 个 CSS 变量见源码

### transition

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| show | 是否展示组件 | boolean | false |
| duration | 动画执行时间，支持统一时长或分别设置进入、离开时长 | number | boolean | Record<string, number> | 300 |
| lazy-render | 是否在首次展示时再渲染内容 | boolean | false |
| name | 动画类型，可选值为 fade、fade-up、fade-down、fade-left、fade-right、slide-up、slide-down、slide-left、slide-right、zoom-in、zoom-out，也支持数组组合多个动画 | TransitionName | TransitionName[] | - |
| destroy | 是否在动画结束后通过 display: none 隐藏内容 | boolean | true |
| enter-class | 进入过渡的开始状态类名 | string | '' |
| enter-active-class | 进入过渡的激活状态类名 | string | '' |
| enter-to-class | 进入过渡的结束状态类名 | string | '' |
| leave-class | 离开过渡的开始状态类名 | string | '' |
| leave-active-class | 离开过渡的激活状态类名 | string | '' |
| leave-to-class | 离开过渡的结束状态类名 | string | '' |
| disable-touch-move | 是否阻止触摸滚动 | boolean | false |
| custom-style | 自定义根节点样式 | string | '' |
| custom-class | 自定义根节点样式类 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| before-enter | 进入前触发 | - |
| enter | 进入时触发 | - |
| after-enter | 进入后触发 | - |
| before-leave | 离开前触发 | - |
| leave | 离开时触发 | - |
| after-leave | 离开后触发 | - |
| click | 点击组件时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 需要应用动画效果的内容 |

### resize

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| custom-style | 自定义根节点样式 | string | '' |
| custom-class | 自定义根节点样式类 | string | '' |
| custom-container-class | 自定义容器样式类 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| resize | 尺寸发生变化时触发 | { width: number; height: number; top: number; right: number; bottom: number; left: number } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 需要监听尺寸变化的内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-resize-container-min-size | $n-1 | 组件级 CSS 变量定义 |
| --wot-resize-wrapper-z-index | -9999 | 容器最小宽高 |
> 完整 2 个 CSS 变量见源码

### config-provider

#### Props

| 参数 | 说明 | 类型 | 默认值 | 作用范围 |
| --- | --- | --- | --- | --- |
| theme | 主题风格，可选值为 light、dark | string | light | 所有 Wot 组件 |
| theme-vars | 自定义主题变量 | ConfigProviderThemeVars | {} | 所有 Wot 组件 |
| button | Button 组件全局配置，支持 size / variant / type / round | { size?: string; variant?: string; type?: string; round?: boolean } | {} | wd-button |
| tag | Tag 组件全局配置，支持 size / variant / round | { size?: string; variant?: string; round?: boolean } | {} | wd-tag |
| custom-class | 根节点自定义样式类 | string | '' | 根节点 |
| custom-style | 根节点自定义样式 | string | '' | 根节点 |

#### Slots

| name | 说明 |
| --- | --- |
| default | 默认插槽，用于包裹需要应用主题的子组件 |

### root-portal

#### Slots

| name | 说明 |
| --- | --- |
| default | 默认插槽，用于渲染传送内容 |

## 导航组件 Navigation

### navbar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| title | 标题文字 | string | - |
| left-text | 左侧文案 | string | - |
| right-text | 右侧文案 | string | - |
| left-arrow | 是否显示左侧箭头 | boolean | false |
| bordered | 是否显示下边框 | boolean | false |
| fixed | 是否固定到顶部 | boolean | false |
| placeholder | 固定在顶部时，在标签位置生成一个等高的占位元素 | boolean | false |
| z-index | 导航栏 z-index | number | 10 |
| safe-area-inset-top | 是否开启顶部安全区适配 | boolean | false |
| left-disabled | 是否禁用左侧按钮，禁用时透明度降低，且无法点击 | boolean | false |
| right-disabled | 是否禁用右侧按钮，禁用时透明度降低，且无法点击 | boolean | false |
| custom-class | 根节点样式类名 | string | - |
| custom-style | 根节点样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click-left | 点击左侧按钮时触发 | - |
| click-right | 点击右侧按钮时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| capsule | 自定义胶囊（当存在胶囊时，left 不生效） |
| left | 左侧内容 |
| title | 标题内容 |
| right | 右侧内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-navbar-height | $n-44 | 导航栏高度 |
| --wot-navbar-bg | $filled-oppo | 导航栏背景色 |
| --wot-navbar-color | $text-main | 标题文字颜色 |
| --wot-navbar-title-font-weight | $font-weight-medium | 标题字重 |
| --wot-navbar-title-font-size | $typography-title-size-main | 标题字号 |
| --wot-navbar-desc-font-size | $typography-body-size-extra-large | 描述区域字号 |
| --wot-navbar-desc-color | $text-main | 描述区域文字颜色 |
| --wot-navbar-arrow-size | $n-24 | 箭头图标大小 |
| --wot-navbar-opacity-disabled | $opacity-disabled | 禁用态透明度 |
| --wot-navbar-left-padding | $padding-zero $padding-loose | 左侧内边距 |
> 完整 11 个 CSS 变量见源码

### tabbar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value / v-model | 选中标签的索引值或者名称 | number | string | 0 |
| fixed | 是否固定在底部 | boolean | false |
| bordered | 是否显示顶部边框 | boolean | false |
| safe-area-inset-bottom | 是否设置底部安全区 | boolean | false |
| shape | 标签栏形状，可选值为 default、round | TabbarShape | 'default' |
| active-color | 激活标签颜色 | string | - |
| inactive-color | 未激活标签颜色 | string | - |
| placeholder | 固定在底部时，是否生成等高占位元素 | boolean | false |
| z-index | 组件层级 | number | 10 |
| before-change | 切换前的回调函数，返回 false 可阻止切换，支持返回 Promise<boolean> | TabbarBeforeChange | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 标签切换时触发 | { value } |

#### Slots

| name | 说明 |
| --- | --- |
| default | TabbarItem 列表 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-tabbar-height | $n-50 | 高度 |
| --wot-tabbar-bg | $filled-oppo | 背景色 |
| --wot-tabbar-z-index | 10 | 固定定位层级 |
| --wot-tabbar-round-margin-horizontal | $spacing-extra-loose | round 变体水平外边距 |
| --wot-tabbar-round-radius | $radius-full | round 变体圆角 |
> 完整 5 个 CSS 变量见源码

### tabs

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前激活项，可为索引或名称 | `number | string` |
| slidable-num | 自动开启滚动的标签数量阈值 | number | 6 |
| map-num | 显示导航地图的标签数量阈值 | number | 10 |
| map-title | 导航地图标题 | string | - |
| sticky | 是否开启粘性布局 | boolean | false |
| offset-top | 吸顶偏移量 | number | 0 |
| swipeable | 是否开启手势滑动 | boolean | false |
| line-theme | 底部条样式，可选值为 normal、text、underline、dot | TabsLineTheme | normal |
| line-width | 底部条宽度 | `number | string` |
| line-height | 底部条高度 | `number | string` |
| color | 激活项文字颜色 | string | '' |
| inactive-color | 非激活项文字颜色 | string | '' |
| animated | 是否开启切换动画 | boolean | false |
| duration | 动画时长，单位毫秒 | number | 300 |
| slidable | 是否开启滚动导航，可选值为 auto、always | TabsSlidable | auto |
| show-scrollbar | 滚动时是否显示滚动条 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 激活项变化时触发 | { index, name } |
| click | 点击页签标题时触发 | { index, name } |
| disabled | 点击禁用页签时触发 | { index, name } |
| update:modelValue | 激活项变化时触发 | `number |

#### Slots

| name | 说明 |
| --- | --- |
| default | Tab 内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| setActive | 设置激活项 | (value: number | string, init: boolean, setScroll: boolean) |
| scrollIntoView | 使选中项滚动到可视区域 | - |
| updateLineStyle | 更新激活项底部条样式 | (animation?: boolean) |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-tabs-nav-bg | $filled-oppo | 导航背景色 |
| --wot-tabs-nav-sticky-width | 100vw | 导航吸顶宽度 |
| --wot-tabs-nav-color | $text-secondary | 导航文字颜色 |
| --wot-tabs-nav-color-active | $primary-6 | 导航激活文字颜色 |
| --wot-tabs-nav-font-weight-active | $font-weight-medium | 导航激活文字字重 |
| --wot-tabs-nav-item-font-size | $typography-body-size-extra-large | 导航项字号 |
| --wot-tabs-nav-item-line-height | $typography-body-line-height-size-extra-large | 导航项行高 |
| --wot-tabs-nav-item-padding | $padding-main $padding-extra-loose | 导航项内边距 |
| --wot-tabs-nav-color-disabled | $text-disabled | 导航禁用文字颜色 |
| --wot-tabs-nav-line-bg | $primary-6 | 导航线背景色 |
> 完整 34 个 CSS 变量见源码

### segmented

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| value / v-model:value | 当前选中的值 | string | number | - |
| disabled | 是否禁用分段器 | boolean | false |
| options | 数据集合 | string[] | number[] | SegmentedOption[] | [] |
| theme | 分段器主题，可选值为 card、outline | string | 'card' |
| vibrate-short | 切换选项时是否触发短振动 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 选项切换时触发 | SegmentedOption |
| click | 选项点击时触发 | SegmentedOption |

#### Slots

| name | 说明 |
| --- | --- |
| label | 选项标签内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| updateActiveStyle | 更新滑块偏移量，参数 animation 用于控制是否启用动画，默认开启 | (animation?: boolean) => void |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-segmented-bg | $filled-content | 组件背景色 |
| --wot-segmented-border-width | $stroke-main | 组件边框宽度 |
| --wot-segmented-padding | $padding-super-tight | 组件内边距 |
| --wot-segmented-radius | $radius-main | 组件圆角 |
| --wot-segmented-item-color | $text-secondary | 选项文字颜色 |
| --wot-segmented-item-font-size | $typography-body-size-extra-large | 选项文字字号 |
| --wot-segmented-item-line-height | $typography-body-line-height-size-extra-large | 选项文字行高 |
| --wot-segmented-item-padding | $padding-ultra-tight $padding-loose | 选项内边距 |
| --wot-segmented-item-font-weight | $font-weight-regular | 选项文字字重 |
| --wot-segmented-item-font-weight-active | $font-weight-medium | 选项激活字重 |
> 完整 23 个 CSS 变量见源码

### sidebar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value / v-model | 当前激活项的值 | string | number | 0 |
| before-change | 切换前钩子，接收目标值，返回 boolean 或 Promise<boolean> | function | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 激活项切换时触发 | { value, label } |

#### Slots

| name | 说明 |
| --- | --- |
| default | SidebarItem 列表 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-sidebar-width | 114px | 侧边栏宽度 |
| --wot-sidebar-height | 100% | 侧边栏高度 |
| --wot-sidebar-bg | $filled-oppo | 侧边栏背景色 |
| --wot-sidebar-padding-bg | $filled-oppo | 内容区背景色 |
| --wot-sidebar-item-font-size | $typography-body-size-main | 选项字号 |
| --wot-sidebar-item-line-height | $typography-body-line-height-size-main | 选项行高 |
| --wot-sidebar-item-color | $text-main | 选项文字颜色 |
| --wot-sidebar-item-padding | $padding-extra-tight $padding-extra-loose | 选项内边距 |
| --wot-sidebar-item-bg | $filled-bottom | 选项背景色 |
| --wot-sidebar-item-min-height | $n-52 | 选项最小高度 |
> 完整 20 个 CSS 变量见源码

### pagination

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前页码 | number | - |
| total-page | 总页数；传入 total 时会优先根据 total 与 page-size 计算 | number | 1 |
| total | 总数据条数 | number | 0 |
| page-size | 每页条数 | number | 10 |
| prev-text | 上一页按钮文字；未设置时使用内置国际化文案 | string | - |
| next-text | 下一页按钮文字；未设置时使用内置国际化文案 | string | - |
| show-icon | 是否展示翻页图标 | boolean | false |
| show-message | 是否展示文字提示 | boolean | false |
| button-variant | 分页按钮风格，可选值为 base、plain、dashed、text | ButtonVariant | text |
| hide-if-one-page | 总页数只有一页时是否隐藏 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 页码变化时触发 | { value } |
| update:modelValue | v-model 更新时触发 | value: number |

#### Slots

| name | 说明 |
| --- | --- |
| prev | 自定义上一页按钮 |
| size | 自定义中间页码显示区域 |
| next | 自定义下一页按钮 |
| message | 自定义底部提示信息，仅在 show-message 为 true 时生效 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-pagination-bg | $filled-oppo | 分页背景色 |
| --wot-pagination-padding | $padding-loose | 分页内边距 |
| --wot-pagination-icon-size | $n-16 | 分页图标尺寸 |
| --wot-pagination-message-color | $text-secondary | 辅助文案颜色 |
| --wot-pagination-message-font-size | $typography-body-size-main | 辅助文案字号 |
| --wot-pagination-message-line-height | $typography-body-line-height-size-main | 辅助文案行高 |
| --wot-pagination-message-margin-top | $spacing-super-tight | 辅助文案顶部间距 |
| --wot-pagination-nav-width | $n-64 | 翻页按钮最小宽度 |
| --wot-pagination-size-font-size | $typography-title-size-main | 页码字号 |
| --wot-pagination-size-line-height | $typography-title-line-height-size-main | 页码行高 |
> 完整 13 个 CSS 变量见源码

### index-bar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| sticky | 索引是否吸顶 | boolean | false |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义索引列表内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-index-bar-sidebar-right | $spacing-super-tight | - |
| --wot-index-bar-index-font-size | $typography-label-size-main | 索引侧边栏右侧偏移 |
| --wot-index-bar-index-width | $typography-label-line-height-size-main | 索引项文字字号 |
| --wot-index-bar-index-height | $typography-label-line-height-size-main | 索引项宽度 |
| --wot-index-bar-index-font-weight | $font-weight-medium | 索引项高度 |
| --wot-index-bar-index-color | $text-auxiliary | 索引项字重 |
| --wot-index-bar-index-border-radius | $radius-full | 索引项文字颜色 |
| --wot-index-bar-index-color-active | $text-white | 索引项圆角 |
| --wot-index-bar-index-bg-active | $primary-6 | 激活索引项文字颜色 |
| --wot-index-bar-current-index-size | $n-56 | 激活索引项背景色 |
> 完整 19 个 CSS 变量见源码

### backtop

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| scroll-top | 页面滚动距离 | number | - |
| text | 按钮文字 | string | - |
| top | 滚动距离超过该值时显示按钮，单位为 px | number | 300 |
| duration | 返回顶部滚动时间，单位为 ms | number | 100 |
| z-index | 组件层级 | number | 10 |
| icon-style | 自定义图标样式 | string | '' |
| shape | 按钮形状，可选值为 circle、square | string | circle |
| bottom | 距离屏幕底部的距离，单位为 px | number | 100 |
| right | 距离屏幕右侧的距离，单位为 px | number | 20 |
| custom-style | 自定义根节点样式 | string | '' |
| custom-class | 自定义根节点样式类 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义按钮内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-backtop-bg | $filled-oppo | 背景色 |
| --wot-backtop-border-color | $border-main | 边框颜色 |
| --wot-backtop-width | $n-48 | 按钮宽度 |
| --wot-backtop-height | $n-48 | 按钮高度 |
| --wot-backtop-radius | $radius-main | 方形模式圆角 |
| --wot-backtop-radius-circle | $radius-full | 圆形模式圆角 |
| --wot-backtop-color | $icon-main | 按钮图标颜色 |
| --wot-backtop-icon-size | $n-20 | 按钮图标尺寸 |
| --wot-backtop-text-font-size | $typography-label-size-main | 文本字号 |
| --wot-backtop-text-color | $text-main | 文本颜色 |
> 完整 12 个 CSS 变量见源码

### tour

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value | 是否显示引导组件，支持 v-model | boolean | false |
| steps | 引导步骤列表 | TourStep[] | [] |
| current | 当前步骤索引，支持 v-model:current | number | 0 |
| mask | 是否显示蒙版 | boolean | true |
| mask-color | 蒙版颜色 | string | - |
| offset | 引导面板与高亮区域的间距 | number | 20 |
| duration | 动画时长，单位毫秒 | number | 300 |
| border-radius | 高亮区域圆角 | number | 4 |
| padding | 高亮区域内边距 | number | 8 |
| prev-text | 上一步按钮文案 | string | 上一步 |
| next-text | 下一步按钮文案 | string | 下一步 |
| skip-text | 跳过按钮文案 | string | 跳过 |
| finish-text | 完成按钮文案 | string | 完成 |
| bottom-safety-offset | 底部安全偏移量，用于滚动计算 | number | 100 |
| top-safety-offset | 顶部安全偏移量，用于滚动计算 | number | 0 |
| custom-nav | 是否启用自定义导航栏模式 | boolean | false |
| click-mask-next | 点击遮罩层是否进入下一步 | boolean | false |
| highlight-style | 默认高亮区域样式 | CSSProperties | {} |
| z-index | 层级 | number | - |
| show-tour-buttons | 是否显示引导按钮 | boolean | true |
| scope | 查询作用域，限定选择器查找范围 | unknown | - |
| missing-strategy | 目标元素缺失时的处理策略，可选值为 skip、stop、hide | MissingStrategy | stop |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 当前步骤变化时触发 | { current: number } |
| prev | 点击上一步时触发 | { prevCurrent, current, total, isElementInTop } |
| next | 点击下一步时触发 | { prevCurrent, current, total, isElementInTop } |
| finish | 点击完成时触发 | { current, total } |
| skip | 点击跳过时触发 | { current, total } |
| error | 目标元素查询失败时触发 | { message, element } |
| update:modelValue | 显隐状态变化时触发 | boolean |
| update:current | 当前步骤变化时触发 | number |

#### Slots

| name | 说明 |
| --- | --- |
| highlight | 自定义高亮区域 |
| content | 自定义引导内容 |
| prev | 自定义上一步按钮 |
| next | 自定义下一步按钮 |
| skip | 自定义跳过按钮 |
| finish | 自定义完成按钮 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| handlePrev | 切换到上一步 | - |
| handleNext | 切换到下一步 | - |
| handleFinish | 完成引导并关闭组件 | - |
| handleSkip | 跳过引导并关闭组件 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-tour-z-index | 999 | 层级 |
| --wot-tour-animation-duration | 0.3s | 动画时长 |
| --wot-tour-highlight-animation-timing | ease-in-out | 高亮动画时间函数 |
| --wot-tour-highlight-shadow-color | $opac-7_55 | 高亮区域阴影颜色 |
| --wot-tour-popover-z-index | 1000 | 弹层层级 |
| --wot-tour-popover-max-width | calc($n-1 * 340) | 弹层最大宽度 |
| --wot-tour-popover-min-width | calc($n-1 * 200) | 弹层最小宽度 |
| --wot-tour-popover-bg | $filled-oppo | 弹层背景色 |
| --wot-tour-popover-padding | $padding-loose | 弹层内边距 |
| --wot-tour-popover-radius | $radius-main | 弹层圆角 |
> 完整 14 个 CSS 变量见源码

## 录入组件 Form/Input

### form

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model | 表单数据对象 | Record<string, any> | - |
| schema | 表单校验对象 | FormSchema | - |
| validate-trigger | 校验触发时机，可选值为 change、blur、submit | string | string[] | submit |
| reset-on-change | 表单数据变化时是否重置表单提示信息（设置为 false 时需要开发者单独对变更项进行校验） | boolean | true |
| error-type | 校验错误提示方式，可选值为 toast、message、none | string | message |
| border | 是否展示边框线 | boolean | false |
| center | 是否使内容垂直居中 | boolean | false |
| size | 单元格大小，可选值为 large | string | - |
| title-width | 左侧标题宽度 | string | number | - |
| layout | 单元格布局方式，可选值为 horizontal、vertical | string | - |
| value-align | 右侧内容对齐方式，可选值为 left、right、center | string | - |
| asterisk-position | 必填星号位置，可选值为 start、end | string | - |
| hide-asterisk | 是否隐藏必填星号 | boolean | false |
| ellipsis | 是否超出隐藏显示省略号 | boolean | false |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击表单项时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| validate | 验证表单，支持传入一个 prop 来验证单个表单项，不传入 prop 时，会验证所有表单项，1.6.0 版本起支持传入数组 | prop?: string | string[] |
| reset | 重置表单项的验证提示 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-form-item-error-message-color | $danger-main | 错误提示文字颜色 |
| --wot-form-item-error-message-font-size | $typography-label-size-main | 错误提示文字字号 |
| --wot-form-item-error-message-line-height | $typography-label-line-height-size-small | 错误提示文字行高 |
| --wot-form-item-error-message-margin-top | $spacing-super-tight | 错误提示顶部外边距 |
| --wot-form-item-placeholder-color | $text-placeholder | 占位符文字颜色 |
> 完整 5 个 CSS 变量见源码

### input

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 输入框绑定值 | string | number | '' |
| type | 输入框类型，可选值为 text、number、digit、idcard、safe-password、nickname、tel | InputType | text |
| placeholder | 占位文本 | string | 请输入... |
| placeholder-style | placeholder 样式，目前支持 color、font-size、font-weight | string | - |
| placeholder-class | placeholder 样式类 | string | '' |
| maxlength | 最大输入长度 | number | 支付宝小程序无默认值，其余平台为 -1 |
| disabled | 是否禁用 | boolean | false |
| readonly | 是否只读 | boolean | false |
| clearable | 是否显示清空按钮 | boolean | false |
| clear-trigger | 清空按钮显示时机，可选值为 focus、always | InputClearTrigger | always |
| focus-when-clear | 点击清空按钮后是否自动聚焦 | boolean | true |
| show-password | 是否显示密码切换按钮 | boolean | false |
| prefix-icon | 前置图标名称 | string | - |
| suffix-icon | 后置图标名称 | string | - |
| show-word-limit | 是否显示字数统计，需要同时设置 maxlength | boolean | false |
| error | 是否展示错误状态 | boolean | false |
| align-right | 输入内容是否右对齐 | boolean | false |
| compact | 是否开启紧凑模式；未显式设置时，在 wd-form-item 中会自动启用 | boolean | - |
| focus | 是否获取焦点 | boolean | false |
| cursor-spacing | 光标与键盘的距离 | number | 0 |
| cursor | 获取焦点时的光标位置 | number | -1 |
| selection-start | 光标起始位置，需与 selection-end 搭配使用 | number | -1 |
| selection-end | 光标结束位置，需与 selection-start 搭配使用 | number | -1 |
| adjust-position | 键盘弹起时是否自动上推页面 | boolean | true |
| hold-keyboard | 聚焦时点击页面是否保持键盘不收起 | boolean | false |
| confirm-type | 键盘右下角按钮文字，可选值为 done、go、next、search、send | InputConfirmType | done |
| confirm-hold | 点击键盘右下角按钮时是否保持键盘不收起 | boolean | false |
| always-embed | 是否强制 input 处于同层状态，仅微信小程序 iOS 生效 | boolean | false |
| ignore-composition-event | 是否忽略文本合成系统事件处理；为 false 时会触发 composition 相关事件，且合成期间会触发 input | boolean | true |
| inputmode | 输入模式提示，可选值为 none、text、decimal、numeric、tel、search、email、url | InputMode | text |
| enable-native | 支付宝小程序下是否启用原生输入框，设为 false 可避免弹起键盘后内容上移 | boolean | true |
| custom-input-class | 输入框自定义类名 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| input | 输入时触发 | { value, cursor, keyCode } |
| focus | 聚焦时触发 | { value, height } |
| blur | 失焦时触发 | { value } |
| clear | 点击清空按钮时触发 | - |
| confirm | 点击键盘完成按钮时触发 | { value } |
| keyboardheightchange | 键盘高度变化时触发 | { height, duration } |
| clickprefixicon | 点击前置图标时触发 | - |
| clicksuffixicon | 点击后置图标时触发 | - |
| click | 点击组件根节点时触发 | event: MouseEvent |

#### Slots

| name | 说明 |
| --- | --- |
| prefix | 自定义前置内容 |
| suffix | 自定义后置内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-input-padding | $padding-loose $padding-loose | 输入框内边距 |
| --wot-input-bg | $filled-oppo | 输入框背景色 |
| --wot-input-inner-height | $n-20 | 输入框内高度 |
| --wot-input-inner-font-size | $typography-body-size-main | 输入框内字号 |
| --wot-input-inner-color | $text-main | 输入框文字颜色 |
| --wot-input-inner-placeholder-color | $text-placeholder | 输入框占位符颜色 |
| --wot-input-disabled-color | $text-disabled | 输入框禁用颜色 |
| --wot-input-icon-color | $icon-secondary | 输入框图标颜色 |
| --wot-input-icon-font-size | $n-18 | 输入框图标字号 |
| --wot-input-icon-margin | $spacing-tight | 输入框图标间距 |
> 完整 15 个 CSS 变量见源码

### textarea

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 文本域绑定值 | string | number | '' |
| placeholder | 占位文本 | string | 请输入... |
| placeholder-style | placeholder 样式 | string | - |
| placeholder-class | placeholder 样式类 | string | '' |
| disabled | 是否禁用 | boolean | false |
| readonly | 是否只读 | boolean | false |
| maxlength | 最大输入长度，设置为 -1 时不限制长度 | number | -1 |
| clearable | 是否显示清空按钮 | boolean | false |
| show-word-limit | 是否显示字数统计，需要同时设置 maxlength | boolean | false |
| clear-trigger | 清空按钮显示时机，可选值为 focus、always | InputClearTrigger | always |
| focus-when-clear | 点击清空按钮后是否自动聚焦 | boolean | true |
| error | 是否展示错误状态 | boolean | false |
| focus | 是否获取焦点 | boolean | false |
| auto-focus | 是否自动聚焦并拉起键盘 | boolean | false |
| auto-height | 是否自动增高输入框高度 | boolean | false |
| fixed | 在 position: fixed 区域时是否启用固定模式 | boolean | false |
| cursor-spacing | 光标与键盘的距离 | number | 0 |
| cursor | 获取焦点时的光标位置 | number | -1 |
| confirm-type | 键盘右下角按钮文字，可选值为 done、go、next、search、send | ConfirmType | - |
| confirm-hold | 点击键盘右下角按钮时是否保持键盘不收起 | boolean | false |
| show-confirm-bar | 是否显示键盘上方带有“完成”按钮的工具栏 | boolean | true |
| selection-start | 光标起始位置，需与 selection-end 搭配使用 | number | -1 |
| selection-end | 光标结束位置，需与 selection-start 搭配使用 | number | -1 |
| adjust-position | 键盘弹起时是否自动上推页面 | boolean | true |
| disable-default-padding | 是否去掉 iOS 下默认内边距 | boolean | false |
| hold-keyboard | 聚焦时点击页面是否保持键盘不收起 | boolean | false |
| ignore-composition-event | 是否忽略文本合成系统事件处理；为 false 时会触发 composition 相关事件，且在合成期间会触发 input | boolean | true |
| inputmode | 输入模式提示，可选值为 none、text、decimal、numeric、tel、search、email、url | InputMode | text |
| enable-native | 支付宝小程序下是否启用原生输入框，设为 false 可避免弹起键盘后内容上移 | boolean | true |
| compact | 是否开启紧凑模式；未显式设置时，在 wd-form-item 中会自动启用 | boolean | - |
| custom-textarea-class | textarea 自定义类名 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| input | 输入时触发 | { value, cursor, keyCode } |
| focus | 聚焦时触发 | { value, height } |
| blur | 失焦时触发 | { value, cursor } |
| clear | 点击清空按钮时触发 | - |
| linechange | 行数变化时触发 | { height, heightRpx, lineCount } |
| confirm | 点击键盘完成按钮时触发 | { value } |
| keyboardheightchange | 键盘高度变化时触发 | { height, duration } |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-textarea-padding | $padding-loose $padding-loose | 文本域内边距 |
| --wot-textarea-bg | $filled-oppo | 文本域背景色 |
| --wot-textarea-inner-min-height | $n-20 | 文本域最小高度 |
| --wot-textarea-inner-font-size | $typography-body-size-main | 文本域字号 |
| --wot-textarea-inner-line-height | $typography-body-line-height-size-main | 文本域行高 |
| --wot-textarea-inner-color | $text-main | 文本域文字颜色 |
| --wot-textarea-inner-placeholder-color | $text-placeholder | 占位符颜色 |
| --wot-textarea-disabled-color | $text-disabled | 禁用态颜色 |
| --wot-textarea-icon-color | $icon-secondary | 图标颜色 |
| --wot-textarea-icon-font-size | $n-18 | 图标字号 |
> 完整 15 个 CSS 变量见源码

### password-input

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前输入值 | string | '' |
| mask | 是否隐藏密码内容 | boolean | true |
| info | 输入框下方文字提示 | string | '' |
| error-info | 输入框下方错误提示 | string | '' |
| gutter | 输入框格子之间的间距，默认单位为 px | number | string | 0 |
| length | 密码长度 | number | 6 |
| focused | 是否处于聚焦状态；聚焦时显示光标，通常与键盘显隐状态联动 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| focus | 点击密码输入框时触发 | event: Event |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-password-input-height | $n-50 | 输入区域高度 |
| --wot-password-input-margin | $n-16 | 组件外边距 |
| --wot-password-input-font-size | $typography-title-size-large | 输入字号 |
| --wot-password-input-line-height | $typography-title-line-height-size-large | 输入行高 |
| --wot-password-input-radius | $n-6 | 输入格圆角 |
| --wot-password-input-bg | $filled-oppo | 输入格背景色 |
| --wot-password-input-info-color | $text-auxiliary | 提示信息颜色 |
| --wot-password-input-info-font-size | $typography-body-size-main | 提示信息字号 |
| --wot-password-input-border-color | $border-main | 边框颜色 |
| --wot-password-input-border-width | $stroke-main | 边框宽度 |
> 完整 19 个 CSS 变量见源码

### keyboard

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model:visible | 是否展开 | boolean | false |
| v-model | 当前输入值 | string | '' |
| title | 键盘标题 | string | - |
| mode | 键盘模式，可选值为 default、custom、car | KeyboardMode | default |
| z-index | 层级 | number | 100 |
| maxlength | 最大输入长度 | number | Infinity |
| show-delete-key | 是否显示删除键；仅对默认数字键盘与右侧栏键盘生效 | boolean | true |
| random-key-order | 是否随机排序数字按键 | boolean | false |
| close-text | 关闭按钮文本；在默认/车牌模式中显示在头部，在 custom 模式中显示在右侧大按钮上 | string | - |
| delete-text | 删除按钮文本 | string | - |
| close-button-loading | 关闭按钮是否显示加载状态 | boolean | false |
| modal | 是否显示蒙层；设为 false 时蒙层透明 | boolean | false |
| hide-on-click-outside | 是否允许点击外部关闭键盘，同时控制是否渲染遮罩 | boolean | true |
| lock-scroll | 是否锁定背景滚动 | boolean | true |
| safe-area-inset-bottom | 是否适配底部安全区 | boolean | true |
| extra-key | 额外按键，可传单个字符串或字符串数组；custom 模式下支持两个额外按键 | string | string[] | - |
| root-portal | 是否从页面中脱离出来，用于解决 fixed 失效问题 | boolean | false |
| v-model:car-lang | 车牌键盘语言模式，当 mode=car 时生效，可选值为 zh、en | CarKeyboardLang | - |
| auto-switch-lang | 是否自动切换车牌键盘语言，当 mode=car 且 car-lang 为非受控状态时生效 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| input | 点击可输入按键时触发 | key: string |
| delete | 点击删除键时触发 | - |
| close | 点击关闭按钮或遮罩关闭时触发 | - |
| update:visible | 键盘显隐状态变化时触发 | visible: boolean |
| update:modelValue | 当前输入值变化时触发 | value: string |
| update:carLang | 车牌键盘语言变化时触发 | lang: 'zh' | 'en' |

#### Slots

| name | 说明 |
| --- | --- |
| title | 自定义标题内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-keyboard-bg | $filled-content | 键盘背景色 |
| --wot-keyboard-color | $text-main | 键盘文字颜色 |
| --wot-keyboard-padding | $padding-tight | 键盘内容间距 |
| --wot-keyboard-header-height | $n-40 | 键盘头部高度 |
| --wot-keyboard-title-color | $text-secondary | 键盘标题颜色 |
| --wot-keyboard-title-font-size | $typography-body-size-main | 键盘标题字号 |
| --wot-keyboard-close-color | $primary-6 | 关闭按钮文字颜色 |
| --wot-keyboard-close-opacity-hover | $opacity-dimmer | 关闭按钮悬浮透明度 |
| --wot-keyboard-close-font-size | $typography-body-size-main | 关闭按钮字号 |
| --wot-keyboard-close-padding | $padding-zero $padding-loose | 关闭按钮内边距 |
> 完整 10 个 CSS 变量见源码

### input-number

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 绑定值 | number | string | - |
| min | 最小值 | number | 1 |
| max | 最大值 | number | Number.MAX_SAFE_INTEGER |
| step | 步进值 | number | 1 |
| step-strictly | 是否严格按步进值递增或递减 | boolean | false |
| precision | 数值精度 | number | string | 0 |
| disabled | 是否禁用 | boolean | false |
| disable-input | 是否禁用输入框 | boolean | false |
| disable-minus | 是否禁用减号按钮 | boolean | false |
| disable-plus | 是否禁用加号按钮 | boolean | false |
| without-input | 是否不显示输入框 | boolean | false |
| input-width | 输入框宽度，支持数字和带单位字符串 | number | string | - |
| allow-null | 是否允许输入值为空，设置为 true 后允许传入空字符串 | boolean | false |
| placeholder | 输入框占位文本 | string | '' |
| adjust-position | 键盘弹起时是否自动上推页面 | boolean | true |
| before-change | 数值变更前触发，返回 false 可阻止值更新，支持返回 Promise<boolean> | (value: number | string) => boolean | Promise<boolean> | - |
| long-press | 是否允许长按进行加减 | boolean | false |
| immediate-change | 是否立即响应输入变化，false 时仅在失焦或按钮点击时更新 | boolean | true |
| update-on-init | 是否在初始化时更新 v-model 为修正后的值 | boolean | true |
| input-type | 输入框类型，可选值为 number、digit | 'number' | 'digit' | digit |
| theme | 主题风格，可选值为 default、outline、outline-split、primary | InputNumberTheme | default |
| round | 是否启用圆角样式 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 值修改时触发 | { value } |
| focus | 输入框获取焦点时触发 | { value, height } |
| blur | 输入框失去焦点时触发 | { value } |
| update:modelValue | v-model 更新时触发 | number | string |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-input-number-action-size | $n-28 | 操作按钮尺寸 |
| --wot-input-number-action-bg | $filled-bottom | 操作按钮背景色 |
| --wot-input-number-action-color | $icon-secondary | 操作按钮颜色 |
| --wot-input-number-action-border-radius | $radius-main | 操作按钮圆角 |
| --wot-input-number-action-border-radius-full | $radius-full | 圆角模式操作按钮圆角 |
| --wot-input-number-action-disabled-color | $icon-disabled | 操作按钮禁用颜色 |
| --wot-input-number-action-divider-width | $stroke-main | 操作按钮分割线宽度 |
| --wot-input-number-action-divider-bg | $filled-oppo | 操作按钮分割线颜色 |
| --wot-input-number-action-divider-split-bg | $filled-strong | 分离模式分割线颜色 |
| --wot-input-number-action-divider-split-height | $n-18 | 分离模式分割线高度 |
> 完整 23 个 CSS 变量见源码

### search

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 输入框内容，双向绑定 | string | '' |
| custom-input-class | 自定义输入框类名 | string | '' |
| placeholder | 搜索框占位文本 | string | '搜索' |
| cancel-txt | 搜索框右侧文本 | string | '取消' |
| variant | 搜索框变体，可选值为 plain、filled、light | string | 'plain' |
| hide-cancel | 是否隐藏右侧文本 | boolean | false |
| disabled | 是否禁用搜索框 | boolean | false |
| maxlength | 原生属性，设置最大长度，-1 表示无限制 | number | string | -1 |
| placeholder-left | placeholder 是否左对齐 | boolean | false |
| focus | 是否自动聚焦 | boolean | false |
| focus-when-clear | 是否在点击清空按钮后聚焦输入框 | boolean | false |
| placeholder-style | 原生属性，指定 placeholder 的样式，目前仅支持 color、font-size 和 font-weight | string | - |
| placeholder-class | 原生属性，指定 placeholder 的样式类 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| focus | 输入框聚焦事件 | { value } |
| blur | 输入框失焦事件 | { value } |
| search | 输入框搜索事件 | { value } |
| clear | 点击清空按钮时触发 | - |
| cancel | 点击右侧文本时触发 | { value } |
| change | 输入框内容变化时触发 | { value } |
| click | 禁用状态下点击组件时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| prefix | 输入框左侧自定义内容 |
| input-suffix | 输入框内部右侧自定义内容 |
| suffix | 输入框右侧自定义内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-search-padding | $padding-tight $padding-loose | 组件整体内边距 |
| --wot-search-input-radius | $radius-full | 输入框圆角 |
| --wot-search-input-height | $n-36 | 输入框高度 |
| --wot-search-field-padding | $padding-zero $padding-loose | 输入框内边距 |
| --wot-search-left-icon-margin-right | $spacing-super-tight | 输入框左侧搜索图标向右间距 |
| --wot-search-input-bg | $filled-bottom | 输入框背景色 (filled) |
| --wot-search-bg | $filled-oppo | 组件容器背景色 (filled) |
| --wot-search-cover-bg | $filled-bottom | 组件覆盖层背景色 (filled) |
| --wot-search-light-bg | $filled-bottom | light 模式容器背景色 |
| --wot-search-light-block-bg | $filled-oppo | light 模式内容块背景色 |
> 完整 30 个 CSS 变量见源码

### checkbox

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 单独使用时的绑定值 | string | number | boolean | false |
| name | 在 CheckboxGroup 中的唯一标识值 | string | number | boolean | '' |
| type | 形状类型，支持 circle / square / button / dot | string | 继承自 CheckboxGroup |
| checked-color | 选中颜色 | string | 继承自 CheckboxGroup |
| unchecked-color | 未选中颜色 | string | 继承自 CheckboxGroup |
| disabled | 是否禁用 | boolean | null | 继承自 CheckboxGroup |
| readonly | 是否只读 | boolean | null | 继承自 CheckboxGroup |
| indeterminate | 是否半选状态（样式优先级最高） | boolean | false |
| true-value | 单独使用时的选中值 | string | number | boolean | true |
| false-value | 单独使用时的非选中值 | string | number | boolean | false |
| size | 尺寸，支持 large | string | 继承自 CheckboxGroup |
| placement | 勾选图标位置，支持 left / right | string | 继承自 CheckboxGroup |
| direction | 布局方向，支持 horizontal / vertical | string | 继承自 CheckboxGroup |
| max-width | 文本最大宽度 | string | - |
| custom-label-class | label 文本自定义类名 | string | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 单独使用时，绑定值变化后触发 | { value } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义文本内容 |
| icon | 自定义图标 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| toggle | 切换当前选中状态 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-checkbox-label-margin | $spacing-tight | - |
| --wot-checkbox-color-checked | $primary-6 | 文字与图标距离 |
| --wot-checkbox-icon-size | $n-22 | 选中颜色 |
| --wot-checkbox-unchecked-color | $coolgrey-3 | 图标尺寸 |
| --wot-checkbox-label-font-size | $typography-body-size-main | 未选中图标颜色 |
| --wot-checkbox-label-color | $text-main | 文字字号 |
| --wot-checkbox-label-line-height | $typography-body-line-height-size-main | 文字颜色 |
| --wot-checkbox-opacity-disabled | $opacity-disabled | 文字行高 |
| --wot-checkbox-horizontal-margin | $spacing-zero $spacing-extra-loose $spacing-zero $spacing-zero | 禁用态透明度 |
| --wot-checkbox-button-margin | $spacing-zero $spacing-extra-loose $spacing-extra-loose $spacing-zero | 水平模式多个单选框距离 |
> 完整 29 个 CSS 变量见源码

### radio

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前选中的值，会自动匹配对应 wd-radio 的 value | string | number | boolean | - |
| type | 单选框类型，可选值为 circle、dot、square、button | string | circle |
| checked-color | 选中状态的图标颜色 | string | - |
| unchecked-color | 未选中状态的图标颜色 | string | - |
| disabled | 是否禁用整组单选框 | boolean | false |
| readonly | 是否只读 | boolean | false |
| placement | 图标位置，可选值为 left、right | string | left |
| direction | 布局方向，可选值为 horizontal、vertical | string | vertical |
| allow-uncheck | 是否允许取消当前已选中的值 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 绑定值变化时触发 | { value } |

#### Slots

| name | 说明 |
| --- | --- |
| wd-radio | default |
| wd-radio | icon |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-radio-label-margin | $spacing-tight | 文案与图标间距 |
| --wot-radio-checked-color | $primary-6 | 选中颜色 |
| --wot-radio-icon-size | $n-22 | 图标尺寸 |
| --wot-radio-unchecked-color | $coolgrey-3 | 未选中图标颜色 |
| --wot-radio-label-font-size | $typography-body-size-main | 文案字号 |
| --wot-radio-label-color | $text-main | 文案颜色 |
| --wot-radio-label-line-height | $typography-body-line-height-size-main | 文案行高 |
| --wot-radio-disabled | $opacity-disabled | 禁用态透明度 |
| --wot-radio-horizontal-margin | $spacing-zero $spacing-extra-loose $spacing-zero $spacing-zero | 水平模式间距 |
| --wot-radio-button-margin | $spacing-zero $spacing-extra-loose $spacing-extra-loose $spacing-zero | 按钮模式项间距 |
> 完整 29 个 CSS 变量见源码

### switch

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value / v-model | 绑定值 | boolean | string | number | false |
| disabled | 是否禁用 | boolean | false |
| inactive-action-icon | 非激活状态操作按钮图标 | string | - |
| active-action-icon | 激活状态操作按钮图标 | string | - |
| active-icon | 激活状态图标，设置后会忽略 active-text | string | - |
| inactive-icon | 非激活状态图标，设置后会忽略 inactive-text | string | - |
| class-prefix | 类名前缀，用于使用自定义图标，用法参考 Icon 组件 | string | 'wd-icon' |
| inactive-action-css-icon | 非激活状态操作按钮 CSS 图标，用法参考 Icon 组件 | boolean | string | false |
| active-action-css-icon | 激活状态操作按钮 CSS 图标，用法参考 Icon 组件 | boolean | string | false |
| active-css-icon | 激活状态 CSS 图标，设置后将忽略 active-text，用法参考 Icon 组件 | boolean | string | false |
| inactive-css-icon | 非激活状态 CSS 图标，设置后将忽略 inactive-text，用法参考 Icon 组件 | boolean | string | false |
| active-text | 激活状态文本 | string | '' |
| inactive-text | 非激活状态文本 | string | '' |
| active-value | 激活值 | boolean | string | number | true |
| inactive-value | 非激活值 | boolean | string | number | false |
| active-color | 激活颜色 | string | - |
| inactive-color | 非激活颜色 | string | - |
| size | 开关大小 | string | number | - |
| shape | 形状，可选值为 round、square | SwitchShape | 'round' |
| loading | 是否显示加载中 | boolean | false |
| loading-props | 加载配置项 | Partial<LoadingProps> | - |
| before-change | 修改前钩子 | SwitchBeforeChange | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 值修改事件 | { value } |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-switch-color-action-bg | $base-white | 操作块背景色 |
| --wot-switch-color-action-icon | $primary-6 | 操作块图标颜色 |
| --wot-switch-color-active-bg | $primary-6 | 激活态背景色 |
| --wot-switch-color-inner-active-text | $text-white | 内部文案激活态颜色 |
| --wot-switch-color-inner-inactive-text | $text-auxiliary | 内部文案非激活态颜色 |
| --wot-switch-color-active-icon | $icon-white | 内部图标激活态颜色 |
| --wot-switch-color-inactive-bg | $filled-strong | 非激活态背景色 |
| --wot-switch-color-inactive-icon | $icon-auxiliary | 非激活态图标颜色 |
| --wot-switch-left-right-padding | $padding-tight | 水平内边距 |
| --wot-switch-top-bottom-padding | $padding-ultra-tight | 垂直内边距 |
> 完整 25 个 CSS 变量见源码

### rate

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前分数 | number | null | null |
| num | 评分最大值 | number | 5 |
| readonly | 是否只读 | boolean | false |
| size | 图标大小 | string | - |
| space | 图标间距 | string | number | - |
| color | 未选中图标颜色 | string | - |
| active-color | 选中图标颜色，支持 string 或 string[] | string | string[] | - |
| icon | 未选中图标类名 | string | 'star-fill' |
| active-icon | 选中图标类名 | string | 'star-fill' |
| disabled | 是否禁用 | boolean | false |
| allow-half | 是否允许半选 | boolean | false |
| clearable | 是否允许再次点击后清除 | boolean | false |
| block | 是否块级显示 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 点击图标修改分值时触发 | { value } |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-rate-color | $filled-extra-strong | 未激活图标颜色 |
| --wot-rate-active-color | $classifyapplication-yellow-content | 激活图标颜色 |
| --wot-rate-size | $n-24 | 图标尺寸 |
| --wot-rate-disabled-opacity | $opacity-disabled | 禁用态透明度 |
| --wot-rate-item-space | $spacing-extra-tight | 图标间距 |
> 完整 5 个 CSS 变量见源码

### slider

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value / v-model | 当前滑块值，单滑块为 number，双滑块为 number[] | SliderValue | 0 |
| min | 最小值 | number | 0 |
| max | 最大值 | number | 100 |
| step | 步长 | number | 1 |
| range | 是否为双向滑块模式 | boolean | false |
| vertical | 是否垂直展示 | boolean | false |
| theme | 滑块风格，可选值为 default、capsule | SliderTheme | 'default' |
| disabled | 是否禁用 | boolean | false |
| show-extreme-value | 是否显示最小值和最大值文本 | boolean | false |
| popover-visible | 气泡显示模式，可选值为 always、normal、never | SliderPopoverVisible | 'normal' |
| marks | 刻度标记，支持数组或对象形式 | SliderMarks | - |
| active-color | 进度条激活态颜色 | string | '' |
| inactive-color | 进度条未激活态颜色 | string | '' |
| custom-class | 根节点样式类 | string | '' |
| custom-style | 根节点样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| dragstart | 开始拖动时触发 | { value } |
| dragmove | 拖动过程中触发 | { value } |
| dragend | 拖动结束时触发 | { value } |
| change | 值变化时触发 | value |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| initSlider | 初始化 slider 尺寸数据 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-slider-bar-height | $n-2 | - |
| --wot-slider-bar-height-capsule | $n-24 | 轨道高度 |
| --wot-slider-bar-bg | $filled-strong | 胶囊轨道高度 |
| --wot-slider-bar-radius | $radius-full | 轨道背景色 |
| --wot-slider-bar-height-vertical | $n-248 | 轨道圆角 |
| --wot-slider-line-bg | $primary-6 | - |
| --wot-slider-line-height | $n-2 | 进度条背景色 |
| --wot-slider-line-height-capsule | $n-18 | 进度条高度 |
| --wot-slider-dot-size | $n-24 | - |
| --wot-slider-dot-bg | $filled-oppo | 滑块尺寸 |
> 完整 44 个 CSS 变量见源码

### picker

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 选中项。单列选择器为长度为 1 的数组，多列则为对应多项值组成的数组 | (string | number)[] | [] |
| v-model:visible | 控制选择器弹出层的显示与隐藏 | boolean | false |
| columns | 选项数据结构数组配置，对于多列使用二维数组，多级联动结合 cascade 嵌套 children | PickerOption[] | PickerOption[][] | [] |
| cascade | 是否开启级联模式 | boolean | false |
| title | 弹出层大标题 | string | - |
| cancel-button-text | 顶部操作栏左侧取消按钮文案 | string | - |
| confirm-button-text | 顶部操作栏右侧确认按钮文案 | string | - |
| value-key | 选项对象中负责标示值的键名 | string | 'value' |
| label-key | 选项对象中负责呈现文本的键名 | string | 'label' |
| children-key | 级联模式中对应的下一级子级键名 | string | 'children' |
| item-height | 内部滚筒每个选项的单个高度（px） | number | 44 |
| visible-item-count | 单屏视窗内最多可见的选项数量 | number | 6 |
| before-confirm | 确定前校验函数，接收 (value) 参数，返回 boolean 或 Promise<boolean> | Function | - |
| close-on-click-modal | 点击遮罩层时是否关闭弹窗 | boolean | true |
| z-index | 弹出层层级深度 | number | 15 |
| safe-area-inset-bottom | 弹出面板是否设置默认的底部安全距离 | boolean | true |
| immediate-change | 是否在手指松开时立即触发 change 而非滚动结束 | boolean | false |
| root-portal | 是否开启 root-portal 将组件脱离当前节点进行渲染 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |
| custom-view-class | pickerView 组件的外部自定义样式类 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| confirm | 点击完成按钮触发 | { value, selectedItems } |
| cancel | 点击取消或蒙层关闭触发 | - |
| open | 打开弹出层选择器时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开 Picker 弹窗 | - |
| close | 关闭 Picker 弹窗 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-picker-toolbar-height | $n-44 | 选择器工具栏高度 |
| --wot-picker-bg | $filled-oppo | 选择器背景色 |
| --wot-picker-action-color-confirm | $primary-6 | 确认按钮文字颜色 |
| --wot-picker-action-color-cancel | $text-secondary | 取消按钮文字颜色 |
| --wot-picker-action-disabled-color | $text-disabled | 操作按钮禁用颜色 |
| --wot-picker-action-font-size | $typography-body-size-extra-large | 操作按钮字号 |
| --wot-picker-title-color | $text-main | 标题文字颜色 |
| --wot-picker-title-font-size | $typography-title-size-main | 标题文字字号 |
| --wot-picker-title-font-weight | $font-weight-medium | 标题文字字重 |
| --wot-picker-title-line-height | $typography-title-line-height-size-main | 标题文字行高 |
> 完整 11 个 CSS 变量见源码

### picker-view

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前选中值；单列时通常为长度为 1 的数组，多列与级联时按列顺序保存各列选中值 | (string | number)[] | [] |
| columns | 选择器数据；可传入一维数组、对象数组、二维数组，级联模式下传入树形数据 | Array<string | number | PickerOption> | Array<Array<string | number | PickerOption>> | [] |
| item-height | 每个选项的高度 | number | 44 |
| visible-item-count | 可见选项数量 | number | 6 |
| value-key | 选项对象中值字段对应的键名 | string | 'value' |
| label-key | 选项对象中文本字段对应的键名 | string | 'label' |
| immediate-change | 是否在手指松开时立即触发 change 事件；若不开启，则在滚动动画结束后触发 | boolean | false |
| cascade | 是否开启级联模式；开启后 columns 应传入树形数据 | boolean | false |
| children-key | 级联模式下子节点字段对应的键名 | string | 'children' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 选中值变化时触发 | { selectedValues, selectedOptions, selectedLabels, selectedIndexes, columnIndex } |
| pickstart | 开始滚动选择时触发 | - |
| pickend | 结束滚动选择时触发 | - |
| update:modelValue | v-model 更新时触发 | value: (string | number)[] |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| getSelectedOptions | 获取所有列选中项 | - |
| getSelectedValues | 获取所有列的选中值 | - |
| getColumnsData | 获取所有列数据 | - |
| getColumnData | 获取指定列数据 | columnIndex: number |
| getColumnIndex | 获取指定列的选中下标 | columnIndex: number |
| getSelectedLabels | 获取所有列选中项的文本 | - |
| getSelectedIndex | 获取所有列的选中下标 | - |
| resetColumns | 重置列数据 | columns: PickerOption[] | PickerOption[][] |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-picker-view-bg | $filled-oppo | 选择器视图背景色 |
| --wot-picker-view-padding | $padding-zero $padding-loose | 选择器视图内边距 |
| --wot-picker-view-mask-start-color | $opacwhite-10_85 | 遮罩渐变起始色 |
| --wot-picker-view-mask-end-color | $opacwhite-5_20 | 遮罩渐变结束色 |
| --wot-picker-view-mask-bg | linear-gradient(180deg, $picker-view-mask-start-color, $picker-view-mask-end-color), linear-gradient(0deg, $picker-view-mask-start-color, $picker-view-mask-end-color) | 遮罩背景渐变 |
| --wot-picker-view-disabled-opacity | $opacity-disabled | 禁用态透明度 |
| --wot-picker-view-column-item-height | $n-44 | 列项高度 |
| --wot-picker-view-column-item-font-size | $typography-body-size-extra-large | 列项字号 |
| --wot-picker-view-column-item-color | $text-main | 列项文字颜色 |
| --wot-picker-view-column-item-padding | 0 $padding-tight | 列项内边距 |
> 完整 14 个 CSS 变量见源码

### select-picker

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 选中项，checkbox 时为数组，radio 时为 string、number 或 boolean | string | number | boolean | (string | number | boolean)[] | - |
| visible / v-model:visible | 控制弹层显示状态 | boolean | false |
| title | 弹出层标题 | string | '选择器' |
| checked-color | 单选框或复选框选中颜色 | string | - |
| min | 最小选中数量，仅 checkbox 生效 | number | 0 |
| max | 最大选中数量，0 表示不限制，仅 checkbox 生效 | number | 0 |
| select-size | 选择器内部选项尺寸 | string | - |
| loading | 是否显示加载状态 | boolean | false |
| loading-color | 加载图标颜色 | string | '#4D80F0' |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | true |
| columns | 选择器数据，一维数组 | Record<string, any>[] | [] |
| type | 选择器类型，可选值为 checkbox、radio | string | 'checkbox' |
| value-key | 选项对象中值字段的 key | string | 'value' |
| label-key | 选项对象中展示文本字段的 key | string | 'label' |
| confirm-button-text | 确认按钮文案 | string | '确认' |
| before-confirm | 确认前校验函数，接收当前选中值，返回 boolean 或 Promise<boolean> | function | - |
| z-index | 弹层层级 | number | 15 |
| safe-area-inset-bottom | 是否适配底部安全区 | boolean | true |
| filterable | 是否支持本地搜索 | boolean | false |
| filter-placeholder | 搜索框占位符 | string | '搜索' |
| scroll-into-view | 重新打开时是否滚动到选中项 | boolean | true |
| custom-content-class | 自定义弹层内容区域类名 | string | '' |
| show-confirm | 是否显示确认按钮，仅 radio 模式生效 | boolean | true |
| root-portal | 是否从页面结构中脱离出来，用于解决 fixed 失效问题 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 选择器内部选项变化时触发 | { value } |
| cancel | 点击关闭按钮或遮罩关闭时触发 | - |
| confirm | 点击确认时触发 | { value, selectedItems } |
| open | 弹层打开时触发 | - |
| close | 弹层关闭时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开弹层 | () => void |
| close | 关闭弹层 | () => void |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-select-picker-bg | $filled-oppo | 选择器背景色 |
| --wot-select-picker-loading-opacity | $opacity-disabled | loading 状态下透明度 |
| --wot-select-picker-loading-icon-size | $n-48 | loading 图标尺寸 |
| --wot-select-picker-wrapper-padding-horizontal | $padding-main | 内容区水平内边距 |
| --wot-select-picker-wrapper-max-height | $n-375 | 内容区最大高度 |
| --wot-select-picker-text-active-color | $primary-6 | 选中态文字颜色 |
| --wot-select-picker-footer-padding | $padding-tight $padding-loose | 底部内边距 |
| --wot-select-picker-footer-border-color | $border-light | 确认区域上边框颜色 |
| --wot-select-picker-item-padding | $padding-extra-loose $padding-zero | 选项内边距 |
> 完整 9 个 CSS 变量见源码

### cascader

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 选中项。静态模式为叶子节点值；异步模式可传路径数组用于回显 | string | number | (string | number)[] | - |
| visible / v-model:visible | 是否显示弹窗 | boolean | false |
| options | 层级选项数据（树形结构） | CascaderOption[] | [] |
| title | 弹出层标题 | string | - |
| before-confirm | 确定前校验函数，参数为 (value, selectedOptions)，返回 boolean 或 Promise<boolean> | CascaderBeforeConfirm | - |
| check-strictly | 是否开启任意级可选 | boolean | false |
| confirm-text | 严格模式下确认按钮文案 | string | '' |
| value-key | 选项值字段名 | string | value |
| text-key | 选项文本字段名 | string | text |
| children-key | 子节点字段名 | string | children |
| tip-key | 提示文案字段名 | string | tip |
| is-leaf-key | 叶子节点标识字段名，值为 true 时点击即确认 | string | isLeaf |
| lazy-load | 异步加载回调，参数为 (option, tabIndex, resolve) | CascaderLazyLoad | - |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | true |
| z-index | 弹窗层级 | number | 15 |
| safe-area-inset-bottom | 弹出面板是否设置底部安全距离（iPhone X 类型机型） | boolean | true |
| line-width | 底部条宽度，支持 number 或带单位字符串 | number | string | - |
| line-height | 底部条高度，支持 number 或带单位字符串 | number | string | - |
| line-theme | 底部条位置样式，支持 normal / text / underline / dot | string | normal |
| root-portal | 是否脱离文档流渲染（H5: teleport，App: renderjs，小程序: root-portal） | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| confirm | 确认选择时触发 | 静态模式：{ value, selectedOptions }，value 为叶子值；异步模式：{ value, selectedOptions }，value 为路径数组 |
| close | 弹窗关闭时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开级联弹窗 | - |
| close | 关闭级联弹窗 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-cascader-bg | $filled-oppo | 级联选择器弹层背景色 |
| --wot-cascader-list-height | calc(336 * #{$n-1}) | 选项列表高度 |
| --wot-cascader-list-item-color | $text-main | 选项文字颜色 |
| --wot-cascader-list-item-opacity-disabled | $opacity-disabled | 选项禁用态透明度 |
| --wot-cascader-list-item-font-size | $typography-body-size-extra-large | 选项文字字号 |
| --wot-cascader-list-item-line-height | $typography-body-line-height-size-extra-large | 选项文字行高 |
| --wot-cascader-list-item-padding | $padding-extra-loose $padding-loose | 选项内边距 |
| --wot-cascader-list-item-color-checked | $primary-6 | 选项选中态文字颜色 |
| --wot-cascader-list-item-font-weight-checked | $font-weight-medium | 选项选中态文字字重 |
| --wot-cascader-list-tip-color | $text-secondary | 选项提示文字颜色 |
> 完整 23 个 CSS 变量见源码

### calendar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 选中值，13 位时间戳或时间戳数组 | number | number[] | null | - |
| v-model:visible | 是否显示弹层 | boolean | false |
| type | 日期类型，支持 date / dates / datetime / week / month / daterange / datetimerange / weekrange / monthrange | string | date |
| min-date | 最小日期时间戳 | number | 当前日期往前 6 个月 |
| max-date | 最大日期时间戳 | number | 当前日期往后 6 个月 |
| first-day-of-week | 周起始日（0 表示周日） | number | 0 |
| formatter | 日期格式化函数 | CalendarFormatter | - |
| max-range | 范围类型的最大可选范围 | number | - |
| range-prompt | 超出最大范围提示文案 | string | - |
| allow-same-day | 范围类型是否允许同一天/同一周/同一月 | boolean | false |
| default-time | 日期默认时分秒 | string | string[] | - |
| time-filter | 时间选项过滤函数（datetime / datetimerange） | CalendarTimeFilter | - |
| hide-second | 是否隐藏秒选择（datetime / datetimerange） | boolean | false |
| title | 弹层标题 | string | 选择日期（内置文案） |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | true |
| z-index | 弹层层级 | number | 15 |
| show-confirm | 是否显示确认按钮 | boolean | true |
| confirm-text | 确认按钮文案 | string | 确定（内置文案） |
| inner-display-format | 自定义范围面板内部回显 | CalendarInnerDisplayFormat | - |
| ellipsis | 范围文案是否省略显示 | boolean | false |
| show-type-switch | 是否显示日周月切换 | boolean | false |
| shortcuts | 快捷选项列表（项需包含 text） | Record<string, any>[] | [] |
| on-shortcuts-click | 快捷选项点击回调 | CalendarOnShortcutsClick | - |
| safe-area-inset-bottom | 是否开启底部安全区 | boolean | true |
| before-confirm | 确认前校验函数 | CalendarBeforeConfirm | - |
| custom-view-class | 面板内部视图类名 | string | '' |
| immediate-change | 是否手指松开即触发时间选择 change 事件 | boolean | false |
| root-portal | 是否脱离页面渲染 | boolean | false |
| panel-height | 可滚动面板高度 | number | 316 |
| show-panel-title | 是否展示滚动面板标题 | boolean | true |
| switch-mode | 切换模式，可选值为none、month、year-month，none 平铺展示所有月份/年份且不展示切换按钮；month 支持按月切换并展示上个月/下个月按钮；year-month 支持按年与按月切换并展示上一年/下一年、上个月/下个月按钮。大跨度日期场景建议使用 month 或 year-month 以降低渲染压力 | string | none |
| duration | 弹层动画时长 | number | 200 |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| confirm | 点击确认后触发 | { value, type } |
| change | 面板日期变化时触发 | { value } |
| cancel | 关闭且未确认时触发 | - |
| open | 日历打开时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| confirm-left | 确认区域左侧插槽 |
| confirm-right | 确认区域右侧插槽 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开日历弹层 | - |
| close | 关闭日历弹层 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-calendar-bg | $filled-oppo | - |
| --wot-calendar-tabs-width | 100% | 日历弹层背景色 |
| --wot-calendar-tabs-padding | $padding-zero $padding-extra-loose | Tabs 宽度 |
| --wot-calendar-shortcuts-padding | $padding-extra-tight $padding-zero | Tabs 内边距 |
| --wot-calendar-tag-spacing-right | $spacing-extra-loose | 快捷选项内边距 |
| --wot-calendar-range-padding | $padding-zero $padding-loose $padding-extra-loose | 标签右间距 |
| --wot-calendar-range-font-size | $typography-body-size-main | 范围标签内边距 |
| --wot-calendar-range-item-font-size | $typography-body-size-extra-large | 范围标签字号 |
| --wot-calendar-range-item-font-weight | $font-weight-medium | 范围标签项字号 |
| --wot-calendar-range-item-line-height | $typography-body-line-height-size-extra-large | 范围标签项字重 |
> 完整 21 个 CSS 变量见源码

### calendar-view

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 选中值，13 位时间戳或时间戳数组 | number | number[] | null | - |
| type | 日期类型，支持 date / dates / datetime / week / month / daterange / datetimerange / weekrange / monthrange | string | date |
| min-date | 最小日期时间戳 | number | 当前日期往前 6 个月 |
| max-date | 最大日期时间戳 | number | 当前日期往后 6 个月 |
| first-day-of-week | 周起始日（0 表示周日） | number | 0 |
| formatter | 日期格式化函数 | CalendarFormatter | - |
| max-range | 范围类型的最大可选范围 | number | - |
| range-prompt | 超出最大范围提示文案 | string | - |
| allow-same-day | 范围类型是否允许同一天/同一周/同一月 | boolean | false |
| show-panel-title | 是否展示面板标题 | boolean | true |
| default-time | 日期默认时分秒 | string | string[] | 00:00:00 |
| panel-height | 可滚动面板高度 | number | 316 |
| time-filter | 时间选项过滤函数（datetime / datetimerange） | CalendarTimeFilter | - |
| time-item-height | 时间选项高度 | number | 44 |
| time-visible-item-count | 时间可见项数量 | number | 3 |
| hide-second | 是否隐藏秒选择（datetime / datetimerange） | boolean | false |
| immediate-change | 是否手指松开即触发时间选择 change 事件 | boolean | false |
| switch-mode | 切换模式，可选值为none、month、year-month，none 平铺展示所有月份/年份且不展示切换按钮；month 支持按月切换并展示上个月/下个月按钮；year-month 支持按年与按月切换并展示上一年/下一年、上个月/下个月按钮。大跨度日期场景建议使用 month 或 year-month 以降低渲染压力 | string | none |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 面板值变化时触发 | { value } |
| pickstart | 时间选择滚动开始时触发 | - |
| pickend | 时间选择滚动结束时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| scrollIntoView | 使当前日期或选中日期滚动到可视区域（面板隐藏到显示时建议调用） | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-calendar-view-panel-title-font-size | $typography-body-size-main | - |
| --wot-calendar-view-panel-title-line-height | $typography-body-line-height-size-main | 面板标题字号 |
| --wot-calendar-view-panel-title-color | $text-main | 面板标题行高 |
| --wot-calendar-view-panel-title-padding | $padding-extra-tight $padding-loose | 面板标题文字颜色 |
| --wot-calendar-view-panel-title-font-weight | $font-weight-medium | 面板标题内边距 |
| --wot-calendar-view-panel-title-margin | $spacing-super-tight $spacing-zero | 面板标题字重 |
| --wot-calendar-view-weeks-padding | $padding-tight $spacing-loose | 面板标题外边距 |
| --wot-calendar-view-week-color | $text-secondary | 星期栏内边距 |
| --wot-calendar-view-week-font-size | $typography-body-size-main | 星期文字颜色 |
| --wot-calendar-view-week-line-height | $typography-body-line-height-size-main | 星期文字字号 |
> 完整 39 个 CSS 变量见源码

### datetime-picker

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model / modelValue | 绑定值。time 类型为字符串；区间模式为数组；其余类型为时间戳 | string | number | Array<string | number> | - |
| visible / v-model:visible | 是否显示弹窗 | boolean | false |
| type | 选择器类型，可选值为 datetime、date、year-month、time、year | string | datetime |
| title | 弹出层标题 | string | - |
| cancel-button-text | 取消按钮文案 | string | - |
| confirm-button-text | 确认按钮文案 | string | - |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | true |
| safe-area-inset-bottom | 弹出面板是否设置底部安全距离 | boolean | true |
| item-height | 单项高度 | number | 44 |
| visible-item-count | 可见项数量 | number | 6 |
| value-key | 选项值字段名 | string | value |
| label-key | 选项文案字段名 | string | label |
| min-date | 最小日期（时间戳） | number | 当前年份前 10 年 1 月 1 日 |
| max-date | 最大日期（时间戳） | number | 当前年份后 10 年 12 月 31 日 23:59:59 |
| min-hour | 最小小时（time 类型生效） | number | 0 |
| max-hour | 最大小时（time 类型生效） | number | 23 |
| min-minute | 最小分钟（time 类型生效） | number | 0 |
| max-minute | 最大分钟（time 类型生效） | number | 59 |
| use-second | 是否显示秒选择，仅 time 和 datetime 生效 | boolean | false |
| min-second | 最小秒数，仅 time 和 datetime 生效 | number | 0 |
| max-second | 最大秒数，仅 time 和 datetime 生效 | number | 59 |
| formatter | 自定义滚筒选项格式化函数 | DatetimePickerViewFormatter | - |
| filter | 自定义过滤函数 | DatetimePickerViewFilter | - |
| before-confirm | 确定前校验函数，接收 (value)，返回 boolean 或 Promise<boolean> | DatetimePickerBeforeConfirm | - |
| display-format-tab-label | 区间模式下自定义 Tab 标签格式化函数 | DatetimePickerDisplayFormatTabLabel | - |
| z-index | 弹窗层级 | number | 15 |
| immediate-change | 是否在手指松开时立即触发 change（仅微信/支付宝小程序） | boolean | false |
| root-portal | 是否脱离文档流渲染（H5: teleport，App: renderjs，小程序: root-portal） | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |
| custom-view-class | pickerView 外部自定义样式类 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开弹窗时触发 | - |
| cancel | 点击取消或关闭弹窗时触发 | - |
| confirm | 点击确认按钮触发 | { value } |
| change | 选中值变化时触发 | { value } |
| toggle | 区间模式切换开始/结束 Tab 时触发 | 当前激活 Tab 对应的值 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开 picker 弹框 | - |
| close | 关闭 picker 弹框 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-datetime-picker-popup-radius | $radius-large $radius-large $radius-zero $radius-zero | 日期时间选择器弹窗圆角 |
| --wot-datetime-picker-title-color | $text-main | 标题文字颜色 |
| --wot-datetime-picker-title-height | $n-56 | 标题区域高度 |
| --wot-datetime-picker-title-font-size | $typography-title-size-main | 标题文字字号 |
| --wot-datetime-picker-title-line-height | $typography-title-line-height-size-main | 标题文字行高 |
| --wot-datetime-picker-title-font-weight | $font-weight-medium | 标题文字字重 |
| --wot-datetime-picker-title-padding | $padding-zero $padding-loose | 标题区域内边距 |
| --wot-datetime-picker-action-font-size | $typography-body-size-large | 操作按钮字号 |
| --wot-datetime-picker-action-line-height | $typography-body-line-height-size-large | 操作按钮行高 |
| --wot-datetime-picker-action-color | $primary-6 | 操作按钮文字颜色 |
> 完整 24 个 CSS 变量见源码

### datetime-picker-view

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model / modelValue | 选中项，time 类型为字符串，其余类型为时间戳 | string | number | - |
| type | 选择器类型，可选值为 datetime、date、year-month、time、year | DateTimeType | datetime |
| item-height | 单项高度 | number | 44 |
| visible-item-count | 可见项数量 | number | 6 |
| value-key | 选项值字段名 | string | value |
| label-key | 选项文案字段名 | string | label |
| formatter | 自定义选项文案格式化函数 | DatetimePickerViewFormatter | - |
| filter | 自定义过滤函数 | DatetimePickerViewFilter | - |
| column-formatter | 自定义列格式化函数 | DatetimePickerViewColumnFormatter | - |
| min-date | 最小日期（时间戳） | number | 当前年份前 10 年 1 月 1 日 |
| max-date | 最大日期（时间戳） | number | 当前年份后 10 年 12 月 31 日 |
| min-hour | 最小小时（time 类型生效） | number | 0 |
| max-hour | 最大小时（time 类型生效） | number | 23 |
| min-minute | 最小分钟（time 类型生效） | number | 0 |
| max-minute | 最大分钟（time 类型生效） | number | 59 |
| use-second | 是否显示秒选择，仅 time 和 datetime 生效 | boolean | false |
| min-second | 最小秒数，仅 time 和 datetime 生效 | number | 0 |
| max-second | 最大秒数，仅 time 和 datetime 生效 | number | 59 |
| immediate-change | 是否在手指松开时立即触发 change（仅微信/支付宝小程序） | boolean | false |
| boundary-min-date | 区间模式开始时间最小边界（用于联动） | number | - |
| boundary-max-date | 区间模式结束时间最大边界（用于联动） | number | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 选中项变化时触发 | { value, columns } |
| pickstart | 滚动开始时触发 | - |
| pickend | 滚动结束时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| getSelectedOptions | 获取当前选中项对象数组 | - |
| correctValue | 纠正并返回合法值 | value: string | number |
| getOriginColumns | 获取原始列定义 | - |

### upload

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| file-list / v-model:file-list | 上传文件列表，例如 [{ url: 'https://xxx.cdn.com/xxx.jpg' }] | UploadFile[] | [] |
| action | 上传地址 | string | '' |
| header | 上传请求头 | Record<string, any> | {} |
| multiple | 是否支持多选文件 | boolean | false |
| disabled | 是否禁用 | boolean | false |
| limit | 最大允许上传个数 | number | - |
| show-limit-num | 限制上传数量时，是否显示当前数量 | boolean | true |
| max-size | 文件大小限制，单位为 byte | number | Number.MAX_VALUE |
| source-type | 选择图片来源 | UploadSourceType[] | ['album', 'camera'] |
| size-type | 所选图片尺寸 | UploadSizeType[] | ['original', 'compressed'] |
| name | 上传文件字段名 | string | file |
| form-data | HTTP 请求附带的额外表单数据 | Record<string, any> | {} |
| on-preview-fail | 预览失败回调 | UploadOnPreviewFail | - |
| before-upload | 上传前置钩子 | UploadBeforeUpload | - |
| before-choose | 选择文件前置钩子 | UploadBeforeChoose | - |
| before-remove | 删除文件前置钩子 | UploadBeforeRemove | - |
| before-preview | 图片预览前置钩子 | UploadBeforePreview | - |
| build-form-data | 动态构建上传 formData 的钩子 | UploadBuildFormData | - |
| loading-type | 加载中图标类型 | LoadingType | circular |
| loading-color | 加载中图标颜色 | string | #ffffff |
| loading-size | 加载中图标尺寸 | string | 24px |
| accept | 接受的文件类型，可选值为 image、video、media、all、file | UploadFileType | image |
| status-key | file 数据结构中状态字段对应的 key | string | status |
| compressed | 是否压缩视频，当 accept 为 video 或 media 时生效 | boolean | true |
| max-duration | 拍摄视频最大时长，单位为秒 | number | 60 |
| camera | 使用前置或后置相机，可选值为 front、back | UploadCameraType | back |
| image-mode | 预览图片的 mode 属性 | ImageMode | aspectFit |
| success-status | 接口响应成功状态码 | number | number[] | 200 |
| custom-evoke-class | 自定义上传按钮样式类 | string | '' |
| custom-preview-class | 自定义预览列表样式类 | string | '' |
| auto-upload | 是否选择文件后自动上传，关闭后需手动调用 submit | boolean | true |
| reupload | 是否开启覆盖上传，开启后会关闭图片预览 | boolean | false |
| upload-method | 自定义上传方法 | UploadMethod | - |
| extension | 根据文件拓展名过滤，H5 支持全部类型过滤，微信小程序支持 all 和 file 场景过滤 | string[] | - |
| custom-class | 根节点自定义样式类 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| success | 上传成功时触发 | UploadSuccessEvent |
| fail | 上传失败时触发 | UploadErrorEvent |
| progress | 上传中时触发 | UploadProgressEvent |
| oversize | 文件大小超过限制时触发 | UploadOversizeEvent |
| chooseerror | 选择文件失败时触发 | any |
| change | 上传列表修改时触发 | UploadChangeEvent |
| remove | 移除文件时触发 | UploadRemoveEvent |
| update:fileList | v-model:file-list 对应的更新事件 | UploadFileItem[] |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义上传唤起区域 |
| preview-cover | 自定义覆盖在预览区域上方的内容，参数为 { file, index } |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| submit | 手动开始上传 | - |
| abort | 取消上传 | (task?: UniApp.UploadTask) => void |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-upload-size | $n-88 | 上传项尺寸 |
| --wot-upload-radius | var(--wot-upload-border-radius, $radius-main) | 上传项圆角 |
| --wot-upload-border-color | $border-main | 上传项边框颜色 |
| --wot-upload-evoke-icon-size | $n-32 | 唤起项的图标大小 |
| --wot-upload-evoke-bg | $filled-bottom | 唤起项背景色 |
| --wot-upload-evoke-color | $text-auxiliary | 唤起项文字颜色 |
| --wot-upload-evoke-font-size | $typography-body-size-main | 唤起项文字大小 |
| --wot-upload-evoke-line-height | $typography-body-line-height-size-main | 唤起项文字行高 |
| --wot-upload-evoke-color-disabled | $text-disabled | 唤起项禁用文字颜色 |
| --wot-upload-evoke-icon-color | $icon-auxiliary | 唤起项图标颜色 |
> 完整 31 个 CSS 变量见源码

### signature

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| pen-color | 签名笔颜色 | string | '#000' |
| line-width | 签名笔宽度 | number | 3 |
| confirm-text | 确认按钮文本 | string | 内置文案 |
| clear-text | 清空按钮文本 | string | 内置文案 |
| revoke-text | 撤回按钮文本 | string | 内置文案 |
| restore-text | 恢复按钮文本 | string | 内置文案 |
| file-type | 导出图片类型，可选值为 png、jpg | 'png' | 'jpg' | 'png' |
| quality | 导出图片质量，取值范围为 0 到 1 | number | 1 |
| export-scale | 导出图片缩放比例 | number | 1 |
| disabled | 是否禁用签名板 | boolean | false |
| height | 画布高度 | string | number | - |
| width | 画布宽度 | string | number | - |
| background-color | 画板背景色 | string | - |
| disable-scroll | 是否禁用画布滚动 | boolean | true |
| enable-history | 是否开启历史记录 | boolean | false |
| step | 撤回和恢复的步长 | number | 1 |
| undo-text | 撤销按钮文本 | string | 内置文案 |
| redo-text | 恢复按钮文本 | string | 内置文案 |
| pressure | 是否开启压感模式 | boolean | false |
| max-width | 压感模式下的最大笔触宽度 | number | 6 |
| min-width | 压感模式下的最小笔触宽度 | number | 2 |
| min-speed | 压感模式下的最小速度阈值 | number | 1.5 |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| start | 开始签名时触发 | event |
| end | 结束签名时触发 | event |
| signing | 签名过程中持续触发 | event |
| confirm | 确认签名时触发 | SignatureResult |
| clear | 清空签名时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| footer | 自定义底部操作区 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| init | 初始化签名板 | forceUpdate?: boolean |
| confirm | 确认并导出签名图片 | - |
| clear | 清空签名 | - |
| restore | 恢复上一步 | - |
| revoke | 撤销上一步 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-signature-bg | $filled-oppo | 签名区域背景色 |
| --wot-signature-radius | $radius-main | 签名区域圆角 |
| --wot-signature-border | $stroke-main solid $border-main | 签名区域边框 |
| --wot-signature-footer-margin-top | $spacing-tight | 底部操作区上间距 |
| --wot-signature-button-margin-left | $spacing-tight | 底部按钮左间距 |
> 完整 5 个 CSS 变量见源码

### slide-verify

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| tolerance | 容错范围，单位为 px | number | 10 |
| text | 提示文字，为空时显示内置文案 | string | '' |
| success-text | 验证成功提示文字，为空时显示内置文案 | string | '' |
| disabled | 是否禁用 | boolean | false |
| background-color | 背景颜色 | string | - |
| active-background-color | 激活时的背景颜色 | string | - |
| icon | 滑块图标名称 | string | double-right |
| success-icon | 成功图标名称 | string | check-circle-fill |
| icon-size | 图标大小 | string / number | - |
| success-icon-size | 成功图标大小 | string / number | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| success | 验证成功时触发 | - |
| fail | 验证失败时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| text | 自定义提示文字内容 |
| success-text | 自定义验证成功提示文字内容 |
| icon | 自定义滑块图标 |
| success-icon | 自定义成功图标 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| init | 初始化尺寸信息 | - |
| reset | 重置验证组件到初始状态 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-slide-verify-height | $n-40 | 组件高度 |
| --wot-slide-verify-width | 100% | 组件宽度 |
| --wot-slide-verify-bg | $filled-bottom | 组件背景色 |
| --wot-slide-verify-radius | $radius-large | 圆角 |
| --wot-slide-verify-track-bg | $success-main | 成功态track背景色 |
| --wot-slide-verify-disabled-opacity | $opacity-disabled | 禁用态透明度 |
| --wot-slide-verify-text-color | $text-main | 提示文字颜色 |
| --wot-slide-verify-text-font-size | $typography-label-size-main | 提示文字字号 |
| --wot-slide-verify-text-line-height | $typography-label-line-height-size-main | 提示文字行高 |
| --wot-slide-verify-success-text-color | $text-white | 成功态文字颜色 |
> 完整 17 个 CSS 变量见源码

## 反馈组件 Feedback

### popup

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 弹出层是否显示 | boolean | false |
| transition | 动画类型，参见 wd-transition 组件的 name | TransitionName | - |
| closable | 是否显示关闭按钮 | boolean | false |
| position | 弹出位置，可选值为 center、top、right、bottom、left | string | center |
| close-on-click-modal | 点击遮罩是否关闭弹出层 | boolean | true |
| duration | 动画持续时间 | number | boolean | 300 |
| modal | 是否显示遮罩 | boolean | true |
| z-index | 弹层层级 | number | 10 |
| hide-when-close | 关闭时是否隐藏弹层节点 | boolean | true |
| modal-style | 自定义遮罩样式 | string | - |
| safe-area-inset-bottom | 底部弹层是否适配底部安全区 | boolean | false |
| lazy-render | 弹层内容是否懒渲染 | boolean | true |
| lock-scroll | 是否锁定背景滚动，锁定后蒙层内内容也将无法滚动 | boolean | true |
| root-portal | 是否从页面结构中脱离出来，用于解决 fixed 失效问题 | boolean | false |
| round | 是否开启圆角，开启后根据弹出位置自动适配（底部弹出→上圆角，顶部弹出→下圆角，居中→四圆角） | boolean | false |
| custom-class | 自定义根节点类名 | string | - |
| custom-style | 自定义弹层样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| close | 弹出层关闭时触发 | - |
| click-modal | 点击遮罩时触发 | - |
| before-enter | 进入前触发 | - |
| enter | 进入时触发 | - |
| after-enter | 进入后触发 | - |
| before-leave | 离开前触发 | - |
| leave | 离开时触发 | - |
| after-leave | 离开后触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 弹层内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-popup-bg | $filled-oppo | 弹窗背景色 |
| --wot-popup-close-color | $icon-auxiliary | 关闭图标颜色 |
| --wot-popup-close-size | $n-24 | 关闭图标尺寸 |
| --wot-popup-close-margin | $spacing-loose | 关闭图标外边距 |
| --wot-popup-radius | $radius-large | 弹窗圆角：根据弹出位置自动适配 |
> 完整 5 个 CSS 变量见源码

### overlay

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| show | 是否展示遮罩层 | boolean | false |
| duration | 动画时长，单位为毫秒 | Record<string, number> | number | boolean | 300 |
| lock-scroll | 是否锁定背景滚动，锁定时蒙层里的内容也将无法滚动 | boolean | true |
| z-index | 层级 | number | 10 |
| custom-style | 根节点样式 | string | '' |
| custom-class | 根节点样式类名 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击遮罩层时触发 | - |
| before-enter | 进入动画开始前触发 | - |
| enter | 进入动画开始时触发 | - |
| after-enter | 进入动画结束后触发 | - |
| before-leave | 离开动画开始前触发 | - |
| leave | 离开动画开始时触发 | - |
| after-leave | 离开动画结束后触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 遮罩层内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-overlay-bg | $opac-7_55 | 遮罩层背景色 |
> 完整 1 个 CSS 变量见源码

### dialog

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| selector | 指定唯一标识，用于区分页面中的多个实例 | string | '' |
| root-portal | 是否脱离页面文档流渲染，用于解决 fixed 失效问题 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| header | 自定义头部区域 |
| image | 自定义图片区域 |
| title | 自定义标题区域 |
| default | 自定义内容区域 |
| actions | 自定义操作区 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| show | 打开弹框 | string | DialogOptions |
| alert | 打开 Alert 弹框 | string | DialogOptions |
| confirm | 打开 Confirm 弹框 | string | DialogOptions |
| prompt | 打开 Prompt 弹框 | string | DialogOptions |
| close | 关闭当前弹框 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-dialog-bg | $filled-oppo | 对话框背景色 |
| --wot-dialog-width | 280px | 对话框宽度 |
| --wot-dialog-radius | $radius-large | 对话框圆角 |
| --wot-dialog-body-padding | $padding-super-loose $padding-super-loose 0 | 对话框主体内边距 |
| --wot-dialog-body-element-spacing | $spacing-tight | 主体元素间距 |
| --wot-dialog-body-element-spacing-large | $spacing-extra-loose | 主体大间距 |
| --wot-dialog-title-color | $text-main | 标题文字颜色 |
| --wot-dialog-title-font-size | $typography-body-size-extra-large | 标题文字字号 |
| --wot-dialog-title-line-height | $typography-body-line-height-size-extra-large | 标题文字行高 |
| --wot-dialog-title-font-weight | $font-weight-medium | 标题文字字重 |
> 完整 43 个 CSS 变量见源码

### action-sheet

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 设置菜单显示隐藏 | boolean | false |
| actions | 菜单选项，详见下方 Action 数据结构 | Action[] | [] |
| panels | 自定义宫格面板项，支持一维 Panel[] 与二维 Panel[][]（多行） | Array<Panel | Panel[]> | [] |
| title | 标题 | string | - |
| cancel-text | 取消按钮文案 | string | - |
| close-on-click-action | 点击选项后是否关闭菜单 | boolean | true |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | true |
| duration | 动画持续时间（ms） | number | 200 |
| z-index | 菜单层级 | number | 10 |
| lazy-render | 弹层内容懒渲染，触发展示时才渲染内容 | boolean | true |
| safe-area-inset-bottom | 弹出面板是否设置底部安全距离（iPhone X 类型机型） | boolean | true |
| root-portal | 是否从页面中脱离出来（H5: teleport，App: renderjs，小程序: root-portal） | boolean | false |
| custom-title-class | 标题区域自定义类名 | string | - |
| custom-class | 根节点自定义类名 | string | - |
| custom-style | 根节点自定义样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| select | 点击选项时触发 | 菜单选项或自定义面板一维数组：{ item, index }；自定义面板二维数组：{ item, rowIndex, colIndex } |
| enter | 打开动画开始时触发 | - |
| after-enter | 打开动画结束时触发 | - |
| leave | 关闭动画开始时触发 | - |
| after-leave | 关闭动画结束时触发 | - |
| close | 面板关闭时触发 | - |
| click-modal | 点击遮罩时触发 | - |
| cancel | 点击取消按钮时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义内容区，传入后会覆盖默认 actions/panels 渲染 |
| close | 自定义标题栏右侧关闭区域 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-action-sheet-bg | $filled-oppo | 底部弹层背景色 |
| --wot-action-sheet-radius | $radius-large | 底部弹层圆角 |
| --wot-action-sheet-action-bg | $filled-oppo | 操作项背景色 |
| --wot-action-sheet-action-font-size | $typography-title-size-main | 操作项字体大小 |
| --wot-action-sheet-action-line-height | $typography-title-line-height-size-main | 操作项行高 |
| --wot-action-sheet-action-color | $text-main | 操作项文字颜色 |
| --wot-action-sheet-action-padding | $padding-extra-loose $padding-loose | 操作项内边距 |
| --wot-action-sheet-gap-bg | $filled-bottom | 取消按钮与操作项的间隔背景色 |
| --wot-action-sheet-cancel-bg | $filled-oppo | 取消按钮背景色 |
| --wot-action-sheet-cancel-font-size | $typography-title-size-main | 取消按钮字体大小 |
> 完整 41 个 CSS 变量见源码

### drop-menu

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| z-index | 弹层层级 | number | 12 |
| direction | 菜单展开方向，可选值为 up、down | DropDirection | 'down' |
| modal | 是否展示蒙层 | boolean | true |
| close-on-click-modal | 是否点击蒙层时关闭 | boolean | true |
| duration | 菜单展开/收起动画时长，单位 ms | number | 200 |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 选中值变化时触发 | { value, selectedItem } |
| open | 菜单开始展开时触发 | - |
| opened | 菜单展开完成时触发 | - |
| close | 菜单开始关闭时触发 | - |
| closed | 菜单关闭完成时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 菜单项容器插槽 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| getShowPop | 获取当前菜单是否展开 | - |
| open | 打开菜单 | - |
| close | 关闭菜单 | - |
| toggle | 切换菜单开关 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-drop-menu-height | $n-48 | 菜单项高度 |
| --wot-drop-menu-list-bg | $filled-oppo | 菜单列表背景色 |
| --wot-drop-menu-option-padding | $padding-loose | 菜单项内边距 |
| --wot-drop-menu-option-opacity-disabled | $opacity-disabled | 菜单项禁用态透明度 |
| --wot-drop-menu-option-padding-horizontal | $padding-extra-loose | 菜单项水平内边距 |
| --wot-drop-menu-arrow-size | $n-16 | 箭头图标大小 |
| --wot-drop-menu-arrow-color | $icon-auxiliary | 箭头图标颜色 |
| --wot-drop-menu-arrow-color-active | $primary-6 | 箭头图标激活态颜色 |
| --wot-drop-menu-arrow-spacing | $spacing-super-tight | 箭头图标间距 |
| --wot-drop-menu-text-color | $text-main | 菜单文字颜色 |
> 完整 14 个 CSS 变量见源码

### popover

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 是否显示气泡 | boolean | false |
| content | 显示内容，normal 模式下为字符串，menu 模式下为 PopoverMenuItem[]，也可以通过 content 插槽传入 | string | PopoverMenuItem[] | - |
| mode | 当前显示模式，可选值为 normal、menu | PopoverMode | normal |
| placement | 弹出位置，可选值为 top、top-start、top-end、bottom、bottom-start、bottom-end、left、left-start、left-end、right、right-start、right-end | PlacementType | bottom |
| offset | 偏移量，支持 number、number[] 或 { x: number, y: number } | PopoverOffset | 0 |
| visible-arrow | 是否显示箭头 | boolean | true |
| disabled | 是否禁用 | boolean | false |
| show-close | 是否显示关闭按钮 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |
| custom-arrow | 箭头节点自定义类名 | string | '' |
| custom-pop | 气泡内容容器自定义类名 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| open | 气泡显示时触发 | - |
| close | 气泡隐藏时触发 | - |
| change | 显隐状态变化时触发 | { show: boolean } |
| menuclick | menu 模式下点击选项时触发 | { item: PopoverMenuItem; index: number } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 触发器区域内容 |
| content | 自定义气泡内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开气泡 | - |
| close | 关闭气泡 | - |
| updatePosition | 重新测量内容尺寸并更新定位 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-popover-bg | $filled-oppo | 浮层背景色 |
| --wot-popover-color | $text-secondary | 浮层文字颜色 |
| --wot-popover-box-shadow | 0 12px 48px 16px rgba(39, 43, 59, 0.03), 0 9px 28px 8px rgba(39, 43, 59, 0.05), 0 5px 12px 4px rgba(39, 43, 59, 0.06) | 浮层阴影 |
| --wot-popover-arrow-box-shadow | 0 12px 48px 16px rgba(39, 43, 59, 0.03), 0 9px 28px 8px rgba(39, 43, 59, 0.05), 0 5px 12px 4px rgba(39, 43, 59, 0.06) | 箭头阴影 |
| --wot-popover-border-color | $border-main | 浮层边框颜色 |
| --wot-popover-radius | $radius-main | 浮层圆角 |
| --wot-popover-arrow-size | $n-8 | 箭头尺寸 |
| --wot-popover-font-size | $typography-body-size-main | 浮层字号 |
| --wot-popover-line-height | $typography-body-line-height-size-main | 浮层行高 |
| --wot-popover-padding | $padding-loose | 浮层内边距 |
> 完整 16 个 CSS 变量见源码

### tooltip

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value | 是否显示提示层，支持 v-model | boolean | false |
| content | 提示内容，也可以通过 content 插槽传入 | string | Array<Record<string, any>> | - |
| placement | 提示位置，可选值为 top、top-start、top-end、bottom、bottom-start、bottom-end、left、left-start、left-end、right、right-start、right-end | PlacementType | bottom |
| offset | 位置偏移量，支持数字、数组或对象配置 | PopoverOffset | 0 |
| visible-arrow | 是否显示箭头 | boolean | true |
| disabled | 是否禁用 | boolean | false |
| show-close | 是否显示关闭按钮 | boolean | false |
| custom-arrow | 箭头自定义类名 | string | '' |
| custom-pop | 提示层自定义类名 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| open | 提示层显示时触发 | - |
| close | 提示层关闭时触发 | - |
| change | 显隐状态变化时触发 | { show: boolean } |
| update:modelValue | 显隐状态变化时触发 | boolean |

#### Slots

| name | 说明 |
| --- | --- |
| default | 触发提示层的目标内容 |
| content | 自定义提示层内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开提示层 | - |
| close | 关闭提示层 | - |
| updatePosition | 重新测量弹层尺寸并更新定位 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-tooltip-bg | $filled-oppo | 背景色 |
| --wot-tooltip-color | $text-secondary | 文字颜色 |
| --wot-tooltip-box-shadow | 0 12px 48px 16px rgba(39, 43, 59, 0.03), 0 9px 28px 8px rgba(39, 43, 59, 0.05), 0 5px 12px 4px rgba(39, 43, 59, 0.06) | 阴影效果 |
| --wot-tooltip-arrow-box-shadow | 0 12px 48px 16px rgba(39, 43, 59, 0.03), 0 9px 28px 8px rgba(39, 43, 59, 0.05), 0 5px 12px 4px rgba(39, 43, 59, 0.06) | 箭头阴影效果 |
| --wot-tooltip-radius | $radius-main | 圆角大小 |
| --wot-tooltip-arrow-size | $n-8 | 箭头大小 |
| --wot-tooltip-font-size | $typography-body-size-main | 字号 |
| --wot-tooltip-padding | $padding-loose | 内边距 |
| --wot-tooltip-close-icon-size | $n-12 | 关闭按钮大小 |
| --wot-tooltip-close-icon-color | $icon-secondary | 关闭按钮颜色 |
> 完整 13 个 CSS 变量见源码

### floating-panel

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model:height | 当前面板的显示高度 | number | 0 |
| anchors | 设置自定义锚点, 单位 px | number[] | [100, windowHeight * 0.6] |
| duration | 动画时长，单位 ms，设置为 0 可以禁用动画 | number | 300 |
| content-draggable | 允许拖拽内容容器 | boolean | true |
| safe-area-inset-bottom | 是否开启底部安全区适配 | boolean | false |
| show-scrollbar | 是否开启滚动条 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| heightChange | 面板显示高度改变且结束拖动后触发 | { height: number } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 默认内容区域插槽 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-floating-panel-bg | $filled-oppo | 面板背景色 |
| --wot-floating-panel-radius | $radius-large | 面板圆角 |
| --wot-floating-panel-z-index | 99 | 面板 z-index |
| --wot-floating-panel-header-height | $n-32 | 头部高度 |
| --wot-floating-panel-bar-width | $n-36 | 拖拽条宽度 |
| --wot-floating-panel-bar-height | $n-4 | 拖拽条高度 |
| --wot-floating-panel-bar-bg | $filled-strong | 拖拽条背景色 |
| --wot-floating-panel-bar-radius | $radius-main | 拖拽条圆角 |
| --wot-floating-panel-content-bg | $filled-oppo | 内容区域背景色 |
> 完整 9 个 CSS 变量见源码

### loading

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| type | 加载指示器类型，可选值为 circular、spinner、dots、wave | LoadingType | circular |
| color | 设置加载指示器颜色 | string | - |
| size | 设置加载指示器大小 | number | string | - |
| text | 加载指示器文字 | string | - |
| direction | 排列方向，可选值为 vertical、horizontal | LoadingDirection | vertical |
| inherit-color | 是否继承父元素颜色 | boolean | false |
| custom-class | 根节点样式类名 | string | - |
| custom-style | 根节点样式 | string | - |
| custom-spinner-class | 自定义加载指示器样式类 | string | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 加载文字内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-loading-color-main | $primary-6 | 主色模式颜色 |
| --wot-loading-color-normal | $icon-secondary | 常规模式颜色 |
| --wot-loading-color-white | $icon-white | 白色模式颜色 |
| --wot-loading-color-text | $text-main | 文本颜色 |
| --wot-loading-size | $n-28 | 组件基准尺寸 |
| --wot-loading-size-width | 1em | 容器宽度 |
| --wot-loading-size-height | 1em | 容器高度 |
| --wot-loading-spinner-circle-stroke-width | 15% | 圆形加载器描边宽度 |
| --wot-loading-spinner-circle-radius | $radius-full | 圆形加载器圆角 |
| --wot-loading-spinner-animation-duration | 0.8s | 旋转动画时长 |
> 完整 24 个 CSS 变量见源码

### progress

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| percentage | 进度数值，最大值为 100 | number | 0 |
| hide-text | 是否隐藏进度文字 | boolean | false |
| color | 进度条颜色，可选值为 string、string[]、ProgressColor[] | string | string[] | ProgressColor[] | - |
| status | 进度条状态，可选值为 success、danger、warning | string | - |
| duration | 进度增加 1% 所需毫秒数 | number | 30 |
| percent-position | 百分比显示位置配置，type 可选 inner、outer，align 可选 left、center、right | PercentPosition | { align: 'right', type: 'outer' } |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-progress-bg | $filled-content | 进度条底色 |
| --wot-progress-danger-color | $danger-main | danger 进度色 |
| --wot-progress-success-color | $success-main | success 进度色 |
| --wot-progress-warning-color | $warning-main | warning 进度色 |
| --wot-progress-color | $primary-6 | 默认进度色 |
| --wot-progress-bar-height | $n-8 | 外部文案模式进度条高度 |
| --wot-progress-height | $n-18 | 内部文案模式组件高度 |
| --wot-progress-label-color | $text-main | 外部文案颜色 |
| --wot-progress-label-width | $n-34 | 外部文案宽度 |
| --wot-progress-label-margin-left | $spacing-tight | 外部文案左侧间距 |
> 完整 18 个 CSS 变量见源码

### circle

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前进度 | number | 0 |
| size | 圆环直径，单位 px | number | 120 |
| color | 进度条颜色，支持字符串或渐变对象 | string | Record<string, string> | #1C64FD |
| layer-color | 轨道颜色 | string | #F2F3F5 |
| fill | 填充颜色 | string | - |
| speed | 动画速度，单位 rate/s | number | 50 |
| text | 圆环中间文字 | string | - |
| stroke-width | 进度条宽度，单位 px | number | 18 |
| stroke-linecap | 端点形状，可选值为 butt、round、square | string | round |
| clockwise | 是否顺时针增加 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义圆环中心内容（仅在 text 为空时显示） |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-circle-text-color | currentColor | 圆环文字颜色 |
| --wot-circle-text-font-size | $typography-title-size-large | 圆环文字字号 |
| --wot-circle-text-line-height | $typography-title-line-height-size-large | 圆环文字行高 |
> 完整 3 个 CSS 变量见源码

### toast

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| selector | 唯一挂载标识，多实例场景下用于区分不同 toast | string | '' |
| msg | 默认提示文案 | string | '' |
| direction | 默认排版方向，可选值为 horizontal、vertical | ToastDirection | horizontal |
| icon-name | 默认图标类型，可选值为 success、error、warning、loading、info | ToastIconType | '' |
| icon-size | 默认图标大小 | number | - |
| loading-type | 默认加载图标类型，可选值为 circular、spinner、dots | ToastLoadingType | circular |
| loading-color | 默认加载图标颜色 | string | #ffffff |
| loading-size | 默认加载图标大小 | number | - |
| icon-color | 默认图标颜色 | string | - |
| position | 默认提示位置，可选值为 top、middle-top、middle、bottom | ToastPositionType | middle |
| z-index | 默认层级 | number | 100 |
| cover | 是否显示透明遮罩层 | boolean | false |
| icon-class | 默认图标类名 | string | '' |
| class-prefix | 类名前缀，用于使用自定义图标，用法参考 Icon 组件 | string | wd-icon |
| css-icon | CSS 图标，用法参考 Icon 组件 | boolean | string | false |
| opened | 完全展示后的回调 | () => void | - |
| closed | 完全关闭后的回调 | () => void | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| show | 展示普通提示 | (options: ToastOptions | string) |
| success | 展示成功提示 | (options: ToastOptions | string) |
| error | 展示错误提示 | (options: ToastOptions | string) |
| warning | 展示警告提示 | (options: ToastOptions | string) |
| info | 展示常规提示 | (options: ToastOptions | string) |
| loading | 展示加载提示 | (options: ToastOptions | string) |
| close | 手动关闭当前提示 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-toast-color | $text-white | 文字颜色 |
| --wot-toast-padding | $padding-extra-loose | 内边距 |
| --wot-toast-max-width | 300px | 最大宽度 |
| --wot-toast-border-radius | $radius-large | 圆角大小 |
| --wot-toast-bg | $opacfilled-tooltip-toast-cover | 背景色 |
| --wot-toast-font-size | $typography-body-size-extra-large | 字号 |
| --wot-toast-line-height | $typography-body-line-height-size-extra-large | 行高 |
| --wot-toast-icon-size | $n-24 | 图标大小 |
| --wot-toast-msg-margin-left | $spacing-tight | 消息内容左边距 |
| --wot-toast-icon-margin-bottom | $spacing-extra-tight | 图标下边距 |
> 完整 12 个 CSS 变量见源码

### notify

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| type | 类型，可选值为 primary success warning danger | NotifyType | danger |
| message | 展示文案，支持通过\n换行 | string | number | - |
| duration | 展示时长(ms)，值为 0 时，notify 不会消失 | number | 3000 |
| visible | 显示状态（支持 v-model） | boolean | false |
| position | 弹出位置，可选值为 top bottom | NotifyPosition | top |
| color | 字体颜色 | string | - |
| background | 背景颜色 | string | - |
| z-index | 将组件的 z-index 层级设置为一个固定值 | number | 99 |
| safe-height | 顶部安全高度 | number | string | - |
| selector | 指定唯一标识 | string | - |
| root-portal | 是否从页面中脱离出来，用于解决各种 fixed 失效问题 | boolean | false |
| closable | 是否显示关闭按钮 | boolean | false |
| variant | 展示形态，可选值为 filled floating | NotifyVariant | filled |
| custom-class | 根节点样式类名 | string | - |
| custom-style | 根节点样式 | string | - |

#### Events

| 事件名 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击时的回调函数 | (event: MouseEvent) => void |
| closed | 关闭时的回调函数 | () => void |
| opened | 展示后的回调函数 | () => void |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义通知内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| showNotify | 展示提示 | NotifyOptions / string |
| closeNotify | 关闭提示 | - |
| setNotifyDefaultOptions | 修改默认配置，影响所有的 showNotify 调用 | NotifyOptions |
| resetNotifyDefaultOptions | 重置默认配置，影响所有的 showNotify 调用 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-notify-text-color | $text-white | 通知文字颜色 |
| --wot-notify-padding | $padding-tight $padding-loose | 通知内边距 |
| --wot-notify-font-size | $typography-body-size-large | 通知文字字号 |
| --wot-notify-line-height | $typography-body-line-height-size-large | 通知文字行高 |
| --wot-notify-primary-bg | $primary-1 | 主色态背景色 |
| --wot-notify-primary-color | $primary-6 | 主色态文字颜色 |
| --wot-notify-success-bg | $success-surface | 成功态背景色 |
| --wot-notify-success-color | $success-main | 成功态文字颜色 |
| --wot-notify-danger-bg | $danger-surface | 危险态背景色 |
| --wot-notify-danger-color | $danger-main | 危险态文字颜色 |
> 完整 20 个 CSS 变量见源码

### notice-bar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| text | 通知栏文案 | string | string[] | '' |
| type | 通知栏类型，可选值为 warning、info、danger | NoticeBarType | warning |
| prefix | 左侧图标名称 | string | - |
| scrollable | 是否开启滚动播放 | boolean | true |
| delay | 滚动动画初始延时，单位为秒 | number | 1 |
| speed | 滚动速度，单位为 px/s | number | 50 |
| closable | 是否显示关闭按钮 | boolean | false |
| wrapable | 是否换行展示；仅在 scrollable=false 的横向模式下生效 | boolean | false |
| color | 文字和图标颜色 | string | - |
| background-color | 背景颜色 | string | - |
| direction | 滚动方向，可选值为 horizontal、vertical | NoticeBarScrollDirection | horizontal |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| close | 点击关闭按钮时触发 | - |
| next | 切换到下一条文本时触发 | index: number |
| click | 点击内容区时触发 | { text: string, index: number } |

#### Slots

| name | 说明 |
| --- | --- |
| prefix | 自定义前置内容；传入 prefix 属性时该插槽不生效 |
| suffix | 自定义后置内容；会覆盖默认关闭图标 |
| default | 自定义通知文本内容；仅在 direction='horizontal' 时生效 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| reset | 重置播放动画 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-notice-bar-padding | $padding-main $padding-loose | 公告栏内边距 |
| --wot-notice-bar-font-size | $typography-body-size-main | 公告栏文字字号 |
| --wot-notice-bar-radius | $radius-zero | 公告栏圆角 |
| --wot-notice-bar-line-height | $typography-body-line-height-size-main | 公告栏文字行高 |
| --wot-notice-bar-warning-bg | $warning-surface | 警告态背景色 |
| --wot-notice-bar-warning-color | $warning-main | 警告态文字颜色 |
| --wot-notice-bar-info-bg | $primary-1 | 信息态背景色 |
| --wot-notice-bar-info-color | $primary-6 | 信息态文字颜色 |
| --wot-notice-bar-danger-bg | $danger-surface | 危险态背景色 |
| --wot-notice-bar-danger-color | $danger-main | 危险态文字颜色 |
> 完整 15 个 CSS 变量见源码

### swipe-action

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 当前滑动状态，可选值为 left、close、right | SwipeActionStatus | close |
| disabled | 是否禁用滑动操作 | boolean | false |
| before-close | 关闭前拦截函数，接收 (reason, position)，返回 true 或 Promise<boolean> 时继续关闭，返回 false 时阻止关闭 | SwipeActionBeforeClose | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 已展开状态下点击内容区或操作区并关闭时触发 | SwipeActionClickEvent |
| update:modelValue | 滑动状态变化时触发 | SwipeActionStatus |

#### Slots

| name | 说明 |
| --- | --- |
| left | 自定义左侧操作区 |
| default | 自定义内容区 |
| right | 自定义右侧操作区 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| close | 关闭已展开的操作按钮 | reason: SwipeActionReason, position?: SwipeActionPosition |

### sort-button

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| model-value / v-model | 当前排序方向值，1 表示升序，0 表示重置状态，-1 表示降序 | SortButtonValue | 0 |
| title | 排序按钮文案 | string | '' |
| allow-reset | 是否允许重置为未选中状态 | boolean | false |
| desc-first | 是否优先切换为降序 | boolean | false |
| line | 是否显示下划线 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 排序方向变化时触发 | { value } |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-sort-button-height | $n-48 | 组件高度 |
| --wot-sort-button-text-font-size | $typography-body-size-main | 文字字号 |
| --wot-sort-button-text-line-height | $typography-body-line-height-size-main | 文字行高 |
| --wot-sort-button-arrow-icon-size | $n-24 | 箭头图标尺寸 |
| --wot-sort-button-right-size | $n-16 | 右侧区域尺寸 |
| --wot-sort-button-icon-color | $icon-auxiliary | 图标颜色 |
| --wot-sort-button-icon-color-active | $primary-6 | 图标激活颜色 |
| --wot-sort-button-color | $text-secondary | 文字颜色 |
| --wot-sort-button-color-active | $text-main | 文字激活颜色 |
| --wot-sort-button-font-weight-active | $font-weight-medium | 文字激活字重 |
> 完整 16 个 CSS 变量见源码

### empty

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| icon | 缺省图标名称或图片 URL | string | empty |
| icon-size | 图标或图片大小，默认单位为 px | string / number | - |
| tip | 提示文案 | string | - |

#### Slots

| name | 说明 |
| --- | --- |
| image | 自定义图片区域内容 |
| bottom | 自定义底部内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-empty-icon-color | $icon-disabled | 图标颜色 |
| --wot-empty-text-font-size | $typography-body-size-extra-large | 文案字号 |
| --wot-empty-text-line-height | $typography-body-line-height-size-extra-large | 文案行高 |
| --wot-empty-text-font-weight | $typography-body-font-weight-strong | 文案字重 |
| --wot-empty-text-color | $text-main | 文案颜色 |
| --wot-empty-text-margin-top | $spacing-loose | 文案顶部间距 |
| --wot-empty-icon-size | $n-98 | 图标尺寸 |
| --wot-empty-padding-bottom | $spacing-ultra-loose | 组件底部内边距 |
> 完整 8 个 CSS 变量见源码

## 展示组件 Display

### count-down

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| time | 倒计时时长，单位毫秒 | number | - |
| millisecond | 是否开启毫秒级渲染 | boolean | false |
| format | 倒计时格式化字符串 | string | HH:mm:ss |
| auto-start | 是否在初始化和重置后自动开始倒计时 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 倒计时变化时触发 | current: TimeData |
| finish | 倒计时结束时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义倒计时展示内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| start | 开始倒计时 | - |
| pause | 暂停倒计时 | - |
| reset | 重置倒计时；若 auto-start 为 true，重置后自动开始 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-count-down-text-color | $text-main | 倒计时文本颜色 |
| --wot-count-down-font-size | $typography-body-size-main | 倒计时文本字号 |
| --wot-count-down-line-height | $typography-body-line-height-size-main | 倒计时文本行高 |
> 完整 3 个 CSS 变量见源码

### count-to

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| color | 文本颜色 | string | '' |
| type | 主题类型，可选值为 default、primary、success、warning、error | string | default |
| start-val | 起始值 | number | 0 |
| end-val | 最终值 | number | 2024 |
| duration | 从起始值到结束值数字变动的时间（毫秒） | number | 3000 |
| auto-start | 是否自动开始 | boolean | true |
| decimals | 保留的小数位数，需大于等于 0 | number | 0 |
| decimal | 小数点符号 | string | . |
| separator | 千分位分隔符 | string | , |
| prefix | 前缀文本 | string | '' |
| suffix | 后缀文本 | string | '' |
| use-easing | 是否使用缓动动画 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| mounted | 组件加载完成时触发 | - |
| finish | 动画完成时触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 主体数字内容 |
| prefix | 前缀内容 |
| suffix | 后缀内容 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| start | 开始动画 | - |
| pause | 暂停动画 | - |
| reset | 重置动画；若 auto-start 为 true，重置后自动开始 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-count-to-text-font-size | $typography-body-size-ultra-large | 数字主体文本字号 |
| --wot-count-to-text-line-height | $typography-body-line-height-size-ultra-large | 数字主体文本行高 |
| --wot-count-to-separator-text-font-size | $typography-label-size-main | 分隔文本字号 |
| --wot-count-to-separator-text-line-height | $typography-label-line-height-size-main | 分隔文本行高 |
> 完整 4 个 CSS 变量见源码

### avatar

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| src | 图片地址 | string | '' |
| text | 文本内容 | string | '' |
| icon | 图标名称，使用 wd-icon 组件 | string | '' |
| size | 头像尺寸，支持 large / medium / normal / small 或自定义数值/单位字符串 | string | number | normal |
| shape | 头像形状，支持 round / square | string | round |
| bg-color | 背景颜色 | string | '' |
| color | 文字颜色 | string | '' |
| alt | 图片加载失败时的占位文本 | string | '' |
| mode | 图片填充模式，同 uni-app image 组件的 mode | string | aspectFill |
| custom-class | 根节点自定义类名 | string | - |
| custom-style | 根节点自定义样式 | string | - |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击头像时触发 | - |
| error | 图片加载失败时触发 | event |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义头像内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-avatar-size-normal | $n-36 | 默认尺寸 |
| --wot-avatar-size-large | $n-56 | 大尺寸 |
| --wot-avatar-size-medium | $n-48 | 中尺寸 |
| --wot-avatar-size-small | $n-24 | 小尺寸 |
| --wot-avatar-text-color | $text-white | 头像文字颜色 |
| --wot-avatar-font-weight | $typography-body-font-weight-strong | 头像文字字重 |
| --wot-avatar-font-size | $typography-title-size-large | 默认文字字号 |
| --wot-avatar-font-size-large | $typography-title-size-extra-large | 大尺寸文字字号 |
| --wot-avatar-font-size-medium | $typography-title-size-large | 中尺寸文字字号 |
| --wot-avatar-font-size-small | $typography-title-size-main | 小尺寸文字字号 |
> 完整 19 个 CSS 变量见源码

### badge

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| value | 显示值 | string | number | - |
| max | 最大值，超过后显示 {max}+，仅当 value 为 number 时生效 | number | - |
| top | 为正时，角标向下偏移对应像素 | string | number | - |
| right | 为正时，角标向左偏移对应像素 | string | number | - |
| is-dot | 红色点状标注 | boolean | false |
| hidden | 隐藏 badge | boolean | false |
| type | 类型，支持 primary / success / warning / danger / info | string | - |
| bg-color | 背景色 | string | - |
| show-zero | 是否显示 0 | boolean | false |
| shape | 徽标形状，支持 circle / square / bubble | string | circle |
| border | 是否显示白色描边 | boolean | false |
| custom-class | 根节点自定义类名 | string | - |
| custom-style | 根节点自定义样式 | string | - |

#### Slots

| name | 说明 |
| --- | --- |
| default | 徽标包裹的内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-badge-height | $n-16 | 徽标高度 |
| --wot-badge-min-width | $n-16 | 徽标最小宽度 |
| --wot-badge-padding | $padding-zero $padding-super-tight | 徽标内边距 |
| --wot-badge-danger-bg | $danger-main | 危险类型背景色 |
| --wot-badge-success-bg | $success-main | 成功类型背景色 |
| --wot-badge-warning-bg | $warning-main | 警告类型背景色 |
| --wot-badge-info-bg | $filled-content | 信息类型背景色 |
| --wot-badge-primary-bg | $primary-6 | 主类型背景色 |
| --wot-badge-color | $text-white | 徽标文本颜色 |
| --wot-badge-info-color | $text-secondary | 信息类型文本颜色 |
> 完整 19 个 CSS 变量见源码

### tag

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| size | 标签尺寸，可选值为 small、medium、large、extra-large、default | TagSize | default |
| type | 标签类型，可选值为 default、primary、success、warning、danger、volcano、lightblue、cyan、pink、purple | TagType | default |
| icon | 左侧图标 | string | '' |
| closable | 是否可关闭 | boolean | false |
| dynamic | 是否为新增标签 | boolean | false |
| color | 文字颜色 | string | '' |
| bg-color | 背景色和边框色 | string | '' |
| round | 是否圆角 | boolean | false |
| mark | 是否标记样式 | boolean | false |
| variant | 标签变体，可选值为 light、dark、plain、dashed、text | TagVariant | dark |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击标签时触发 | MouseEvent |
| close | 点击关闭按钮时触发 | MouseEvent |
| confirm | 新增标签输入确认后触发 | { value: string } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 标签内容 |
| icon | 自定义图标 |
| add | 自定义新增标签内容，dynamic 为 true 时生效 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-tag-color | $text-white | 基础文字颜色 |
| --wot-tag-font-size | $typography-label-size-main | 基础字号 |
| --wot-tag-radius | $radius-small | 默认圆角 |
| --wot-tag-round-radius | $radius-full | 圆角标签圆角 |
| --wot-tag-mark-radius | $radius-large $radius-main | 标记标签圆角 |
| --wot-tag-icon-spacing | $spacing-ultra-tight | 图标间距 |
| --wot-tag-input-width | $n-64 | 输入态宽度 |
| --wot-tag-primary-light-bg | $primary-1 | 主色浅色背景 |
| --wot-tag-primary-bg | $primary-6 | 主色背景 |
| --wot-tag-primary-color | $primary-6 | 主色文字颜色 |
> 完整 62 个 CSS 变量见源码

### card

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| title | 卡片标题 | string | - |
| type | 卡片类型，支持 rectangle | string | - |
| custom-title-class | 标题区域自定义类名 | string | '' |
| custom-content-class | 内容区域自定义类名 | string | '' |
| custom-footer-class | 底部区域自定义类名 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | 卡片内容 |
| title | 卡片标题 |
| footer | 底部操作内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-card-bg | $filled-oppo | 卡片背景色 |
| --wot-card-border-color | $border-light | 卡片边框颜色 |
| --wot-card-radius | $radius-large | 卡片圆角 |
| --wot-card-shadow | $n-0 $n-2 $n-8 $opac-2_04 | 卡片阴影 |
| --wot-card-shadow-hover | $n-0 $n-4 $n-16 $opac-3_08 | 卡片悬浮阴影 |
| --wot-card-transition | box-shadow 0.3s ease | 过渡动画 |
| --wot-card-margin-bottom | $spacing-extra-loose | 卡片底部间距 |
| --wot-card-margin-horizontal | $spacing-extra-loose | 卡片水平方向外边距 |
| --wot-card-title-color | $text-main | 标题颜色 |
| --wot-card-title-font-size | $typography-title-size-main | 标题字号 |
> 完整 20 个 CSS 变量见源码

### divider

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| color | 自定义颜色，支持所有合法颜色值 | string | - |
| content-position | 内容位置，可选值为 left、center、right | DividerPosition | 'center' |
| dashed | 是否显示为虚线 | boolean | false |
| vertical | 是否显示为垂直分割线 | boolean | false |
| hairline | 是否显示为 0.5px 细线 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | 分割线内容，仅在 vertical 为 false 时生效 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-divider-color-default | $text-auxiliary | 默认文字颜色 |
| --wot-divider-color-line | $border-main | 分割线颜色 |
| --wot-divider-padding | $padding-zero $padding-extra-loose | 组件水平内边距 |
| --wot-divider-margin | $spacing-extra-loose $spacing-zero | 组件垂直外边距 |
| --wot-divider-line-height | $n-3 | 分割线厚度 |
| --wot-divider-size-line-height | $typography-body-line-height-size-main | 内容文字行高 |
| --wot-divider-size-vertical-height | $spacing-extra-loose | 纵向分割线高度 |
| --wot-divider-size-vertical-line-width | $n-3 | 纵向分割线宽度 |
| --wot-divider-size-content-left-width | 10% | 左对齐模式左侧线段宽度 |
| --wot-divider-size-content-right-width | 10% | 右对齐模式右侧线段宽度 |
> 完整 16 个 CSS 变量见源码

### gap

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| height | 间隔槽高度，支持数字（单位 px）或字符串（如 20rpx） | string | number | 14 |
| bg-color | 背景颜色 | string | 'transparent' |
| safe-area-bottom | 是否开启底部安全区适配 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

### grid

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| column | 列数 | number | - |
| border | 是否显示边框 | boolean | false |
| gutter | 格子之间的间距，默认单位为 px | number | - |
| square | 是否将格子固定为正方形 | boolean | false |
| clickable | 是否开启格子点击反馈 | boolean | false |
| bg-color | 背景颜色设置 | string | - |
| hover-class | 指定 grid-item 按下去的样式类 | string | 'wd-grid-item__content--hover' |
| center | 是否将格子内容居中显示 | boolean | true |
| direction | 格子内容排列的方向，可选值为 horizontal、vertical | string | 'vertical' |
| reverse | 是否调换图标和文本的位置 | boolean | false |
| icon-size | 图标大小，默认单位为 px | string | '28px' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击(跳转)事件 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-grid-item-font-size | $typography-body-size-extra-large | 网格项文字字号 |
| --wot-grid-item-line-height | $typography-body-line-height-size-extra-large | 网格项文字行高 |
| --wot-grid-item-bg | $filled-oppo | 网格项背景色 |
| --wot-grid-item-padding | $padding-loose $padding-loose | 网格项内容内边距 |
| --wot-grid-item-hover-bg | $feedback-hover | 网格项悬浮背景色 |
| --wot-grid-item-icon-size | $n-32 | 网格项图标尺寸 |
| --wot-grid-item-icon-color | $icon-secondary | 网格项图标颜色 |
| --wot-grid-item-border-width | $stroke-main | 网格项边框宽度 |
| --wot-grid-item-border-color | $border-main | 网格项边框颜色 |
| --wot-grid-item-text-color | $text-main | 网格项文字颜色 |
> 完整 11 个 CSS 变量见源码

### collapse

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| name | 折叠栏唯一标识符 | string | - |
| title | 折叠栏标题，支持 title 插槽覆盖 | string | '' |
| disabled | 是否禁用该折叠栏 | boolean | false |
| before-expend | 打开前拦截函数，接收 name 参数，返回 boolean 或 Promise<boolean> | CollapseItemBeforeExpand | - |
| border | 是否显示边框 | boolean | true |
| custom-body-class | 折叠栏内容容器自定义类名 | string | '' |
| custom-body-style | 折叠栏内容容器自定义样式 | string | '' |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| change | 绑定值变化时触发 | { value } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 面板内容或面板项列表 |
| more | 查看更多模式下自定义展开收起区域 |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| toggleAll | 切换所有面板展开状态。传 true 全部展开，false 全部收起，不传为全部切换 | options?: CollapseToggleAllOptions |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-collapse-bg | $filled-oppo | 折叠面板背景颜色 |
| --wot-collapse-body-color | $text-auxiliary | 折叠内容文字颜色 |
| --wot-collapse-body-font-size | $typography-label-size-large | 折叠内容文字字号 |
| --wot-collapse-padding | $padding-loose | 折叠容器左右内边距 |
| --wot-collapse-body-padding | $padding-extra-loose $padding-loose | 折叠内容区域内边距 |
| --wot-collapse-title-color | $text-main | 折叠标题文字颜色 |
| --wot-collapse-title-font-size | $typography-body-size-extra-large | 折叠标题文字字号 |
| --wot-collapse-arrow-size | $n-16 | 折叠箭头尺寸 |
| --wot-collapse-arrow-color | $icon-main | 折叠箭头颜色 |
| --wot-collapse-more-font-size | $typography-body-size-main | 更多按钮文字字号 |
> 完整 23 个 CSS 变量见源码

### steps

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| active | 当前激活步骤下标 | number | 0 |
| vertical | 是否垂直方向展示 | boolean | false |
| dot | 是否使用点状步骤条 | boolean | false |
| space | 步骤条间距，默认自动计算 | string | '' |
| align-center | 是否水平居中，只对横向步骤条有效 | boolean | false |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | Step 列表 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-step-content-margin-top | $spacing-super-tight | 内容区距头部的顶部外边距 |
| --wot-step-vertical-content-margin-left | $n-32 | 垂直布局时内容区的左外边距 |
| --wot-step-vertical-content-padding-bottom | $n-20 | 垂直布局时内容区的底部内边距 |
| --wot-step-vertical-header-dot-top | $n-6 | 垂直布局且为点状指示器时，头部的顶部绝对定位距离 |
| --wot-step-indicator-size | $n-18 | 圆形指示器的宽高尺寸 |
| --wot-step-circle-border-radius | $radius-full | 圆形指示器的圆角大小 |
| --wot-step-circle-bg | $filled-strong | 等待 (wait) 状态下圆形指示器的背景色 |
| --wot-step-process-bg | $primary-6 | 进行中 (process) 状态下圆形指示器的背景色 |
| --wot-step-finished-bg | $primary-1 | 已完成 (finished) 状态下圆形指示器的背景色 |
| --wot-step-error-bg | $danger-main | 错误 (error) 状态下圆形指示器的背景色 |
> 完整 45 个 CSS 变量见源码

### sticky

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| z-index | 层级 | number | 1 |
| offset-top | 吸顶距离 | number | 0 |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Slots

| name | 说明 |
| --- | --- |
| default | 需要吸顶的内容 |

### skeleton

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| theme | 骨架屏主题，可选值为 text、avatar、paragraph、image | SkeletonTheme | 'text' |
| row-col | 自定义每行占位配置，用于设置行列数量、宽高、间距、圆角和占位类型等，支持数字、对象、对象数组 | SkeletonRowCol[] | [] |
| loading | 是否显示骨架屏 | boolean | true |
| animation | 动画类型，可选值为 gradient、flashed | SkeletonAnimation | '' |
| customClass | 自定义样式类名 | string | string[] | Record<string, boolean> | '' |
| custom-style | 自定义内联样式 | CSSProperties | {} |

#### Slots

| name | 说明 |
| --- | --- |
| default | loading 结束后展示的实际内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-skeleton-row-margin-bottom | $spacing-loose | 行间距 |
| --wot-skeleton-col-bg | $filled-strong | 占位块背景色 |
| --wot-skeleton-text-height | $n-16 | 文本占位高度 |
| --wot-skeleton-rect-height | $n-16 | 矩形占位高度 |
| --wot-skeleton-circle-height | $n-48 | 圆形占位尺寸 |
| --wot-skeleton-text-border-radius | $radius-small | 文本占位圆角 |
| --wot-skeleton-rect-border-radius | $radius-main | 矩形占位圆角 |
| --wot-skeleton-circle-border-radius | $radius-full | 圆形占位圆角 |
| --wot-skeleton-animation-gradient | $filled-bottom | 渐变动画高亮色 |
| --wot-skeleton-animation-flashed | $filled-bottom | 闪烁动画高亮色 |
> 完整 11 个 CSS 变量见源码

### loadmore

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| state | 加载状态，可选值为 loading、finished、error | LoadMoreState | - |
| loading-text | 加载状态文案 | string | 国际化文案“正在努力加载中...” |
| finished-text | 加载完成文案 | string | 国际化文案“已加载完毕” |
| error-text | 加载失败文案；“点击重试”为组件内独立提示文案，不包含在该属性默认值中 | string | 国际化文案“加载失败” |
| loading-props | 内部 wd-loading 的属性配置，类型参见下方 LoadingProps | Partial<LoadingProps> | - |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| reload | state 为 error 时，点击组件触发 | - |

#### Slots

| name | 说明 |
| --- | --- |
| loading | 自定义加载中内容 |
| finished | 自定义加载完成内容 |
| error | 自定义加载失败内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-loadmore-height | $n-48 | 组件最小高度 |
| --wot-loadmore-padding | $spacing-main $spacing-loose | 组件内边距 |
| --wot-loadmore-color | $text-secondary | 默认文字颜色 |
| --wot-loadmore-font-size | $typography-body-size-main | 默认文字字号 |
| --wot-loadmore-line-height | $typography-body-line-height-size-main | 默认文字行高 |
| --wot-loadmore-loading-size | $n-28 | 加载图标尺寸 |
| --wot-loadmore-loading-spacing | $spacing-tight | 加载图标与文字间距 |
| --wot-loadmore-refresh-color | $primary-6 | 刷新态文字颜色 |
| --wot-loadmore-refresh-font-size | $typography-body-size-extra-large | 刷新图标字号 |
| --wot-loadmore-refresh-line-height | $typography-body-line-height-size-extra-large | 刷新图标行高 |
> 完整 11 个 CSS 变量见源码

### img

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| src | 图片链接 | string | - |
| preview-src | 预览图片链接 | string | - |
| width | 宽度，支持数字（单位 px）或字符串 | string | number | - |
| height | 高度，支持数字（单位 px）或字符串 | string | number | - |
| mode | 填充模式，可选值为 top left、top right、bottom left、bottom right、right、left、center、bottom、top、heightFix、widthFix、scaleToFill、aspectFit、aspectFill | ImageMode | scaleToFill |
| round | 是否显示为圆形 | boolean | false |
| radius | 圆角大小，默认单位为 px | string | number | - |
| lazy-load | 是否开启图片懒加载 | boolean | false |
| enable-preview | 是否支持点击预览 | boolean | false |
| show-menu-by-longpress | 是否开启长按图片显示识别小程序码菜单，仅微信小程序平台有效 | boolean | false |
| show-loading | 是否展示默认加载态 | boolean | true |
| show-error | 是否展示默认失败态 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |
| custom-image | 内部 image 节点自定义类名 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击图片时触发 | event: MouseEvent |
| load | 图片加载完成时触发，返回图片加载事件对象 | event: Event |
| error | 图片加载失败时触发，返回图片错误事件对象 | event: Event |

#### Slots

| name | 说明 |
| --- | --- |
| loading | 自定义图片加载中的展示内容 |
| error | 自定义图片加载失败后的展示内容 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-img-border-radius | $radius-full | 圆形模式圆角 |
| --wot-img-loading-color | $text-placeholder | 加载态文字颜色 |
| --wot-img-loading-bg | $filled-bottom | 加载态背景色 |
| --wot-img-loading-font-size | $typography-body-size-main | 加载态文字字号 |
| --wot-img-loading-icon-color | $text-placeholder | 加载态图标颜色 |
| --wot-img-loading-icon-size | $n-32 | 加载态图标尺寸 |
| --wot-img-error-color | $text-placeholder | 失败态文字颜色 |
| --wot-img-error-bg | $filled-bottom | 失败态背景色 |
| --wot-img-error-font-size | $typography-body-size-main | 失败态文字字号 |
| --wot-img-error-icon-color | $text-placeholder | 失败态图标颜色 |
> 完整 11 个 CSS 变量见源码

### image-preview

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| selector | 选择器 | string | - |
| images | 图片 URL 数组 | string[] | [] |
| start-position | 起始位置索引 | number | 0 |
| show-index | 是否显示页码 | boolean | true |
| loop | 是否循环播放 | boolean | true |
| closeable | 是否显示关闭按钮 | boolean | true |
| close-icon | 关闭图标名称 | string | close |
| close-icon-position | 关闭图标位置，可选值为 top-left、top-right | string | top-right |
| close-on-click | 是否点击图片或遮罩时关闭 | boolean | true |
| show-menu-by-longpress | 开启长按图片显示识别小程序码菜单 | boolean | true |
| z-index | 层级 | number | 1000 |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开预览时触发 | - |
| close | 关闭预览时触发 | - |
| change | 切换图片时触发 | { index: number } |
| long-press | 长按图片时触发 | { image: string } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 底部自定义内容，参数为 { current: number } |
| indicator | 自定义指示器，参数为 { current: number, total: number } |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开图片预览 | options?: ImagePreviewOptions | string[] |
| close | 关闭图片预览 | - |
| setActive | 切换到指定图片 | index: number |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-image-preview-bg | $opac-9_75 | 预览蒙层背景色 |
| --wot-image-preview-index-color | $text-white | 页码文字颜色 |
| --wot-image-preview-index-margin | $spacing-extra-loose | 页码顶部间距 |
| --wot-image-preview-index-font-size | $typography-title-size-main | 页码文字字号 |
| --wot-image-preview-index-font-weight | $typography-title-font-weight-main | 页码文字字重 |
| --wot-image-preview-index-line-height | $typography-title-line-height-size-main | 页码文字行高 |
| --wot-image-preview-close-size | $n-24 | 关闭图标尺寸 |
| --wot-image-preview-close-color | $icon-white | 关闭图标颜色 |
| --wot-image-preview-close-margin | $spacing-extra-loose | 关闭按钮边距 |
> 完整 9 个 CSS 变量见源码

### video-preview

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| selector | 实例标识，用于区分多个视频预览实例 | string | '' |
| z-index | 预览层级 | number | 1000 |
| full-screen | 是否全屏预览 | boolean | false |
| close-position | 关闭按钮位置，可选值为 left-top、right-top | string | left-top |
| on-open | 组件打开时的回调 | () => void | - |
| on-close | 组件关闭时的回调 | () => void | - |
| custom-style | 自定义根节点样式 | string | '' |
| custom-class | 自定义根节点样式类 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开预览时触发 | - |
| close | 关闭预览时触发 | - |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| open | 打开视频预览 | video: VideoPreviewOptions |
| close | 关闭视频预览 | - |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-video-preview-bg | $opac-9_75 | 预览背景色 |
| --wot-video-preview-close-size | $n-24 | 关闭图标尺寸 |
| --wot-video-preview-close-color | $icon-white | 关闭图标颜色 |
| --wot-video-preview-close-margin | $spacing-extra-loose | 关闭按钮外边距 |
| --wot-video-preview-video-height | $n-248 | 视频预览区域高度 |
| --wot-video-preview-z-index | 1000 | 预览层级 |
| --wot-video-preview-close-z-index | 10 | 关闭按钮层级 |
> 完整 7 个 CSS 变量见源码

### img-cropper

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model | 打开图片裁剪组件 | boolean | false |
| img-src | 图片资源链接 | string | - |
| img-width | 截屏预览图片的初始宽度; 1、设置宽度不设置高度，按照宽度等比缩放；2、如果都不设置，预览时图片大小会根据裁剪框大小进行等比缩放，进行锁边处理；; string 类型只支持 % 单位，number 类型时单位为 px | number | string | - |
| img-height | 截屏预览图片的初始高度; 1、设置高度不设置宽度，按照高度等比缩放；2、如果都不设置，预览时图片大小会根据裁剪框大小进行等比缩放，进行锁边处理；; string 类型只支持 % 单位，number 类型时单位为 px | number | string | - |
| disabled-rotate | 禁止图片旋转 | boolean | false |
| export-scale | 设置导出图片尺寸 | number | 2 |
| max-scale | 最大缩放倍数 | number | 3 |
| cancel-button-text | 取消按钮文案 | string | 取消 |
| confirm-button-text | 确认按钮文案 | string | 完成 |
| quality | 生成的图片质量 wx.canvasToTempFilePath属性介绍 | number | 1 |
| file-type | 目标文件的类型，wx.canvasToTempFilePath属性介绍 | string | png |
| aspect-ratio | 裁剪框宽高比，格式为 width:height | string | 1:1 |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| confirm | 完成截图时触发 | {tempFilePath, width, height} 分别为生成文件的临时路径 (本地路径)、生成图片宽、生成图片高 |
| cancel | 当取消截图时触发 | - |
| imgloaderror | 当图片加载错误时触发 | {err} |
| imgloaded | 当图片加载完成时触发 | {res} |

#### Methods

| 方法名 | 说明 | 参数 |
| --- | --- | --- |
| setRotate | 设置图片旋转角度 | deg (设置的旋转角度) |
| resetImg | 重置图片的角度、缩放、位置 | - |
| revertIsAnimation | 切换图片过渡动画效果 | animation (是否启用动画) |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-img-cropper-bg | $base-black | 组件背景色 |
| --wot-img-cropper-z-index | 1 | 组件层级 |
| --wot-img-cropper-wrapper-bg | $opac-7_45 | 包裹器蒙层背景色 |
| --wot-img-cropper-overlay-bg | $opac-10_85 | 裁剪区遮罩背景色 |
| --wot-img-cropper-overlay-bg-highlight | $base-transparent | 裁剪区遮罩高亮背景色 |
| --wot-img-cropper-overlay-transition-duration | 0.2s | 遮罩过渡时长 |
| --wot-img-cropper-cut-z-index | 9 | 裁剪框层级 |
| --wot-img-cropper-cut-border-color | $base-white | 裁剪框边框颜色 |
| --wot-img-cropper-corner-size | $stroke-bold | 角标粗细 |
| --wot-img-cropper-corner-length | $n-20 | 角标长度 |
> 完整 22 个 CSS 变量见源码

### swiper

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| autoplay | 是否自动播放 | boolean | true |
| v-model:current | 当前轮播项索引 | number | 0 |
| direction | 轮播方向，可选值为 horizontal、vertical | DirectionType | horizontal |
| display-multiple-items | 同时显示的滑块数量 | number | 1 |
| duration | 滑动动画时长，单位为 ms | number | 300 |
| easing-function | 切换缓动动画类型，可选值为 default、linear、easeInCubic、easeOutCubic、easeInOutCubic | EasingType | default |
| height | 轮播高度 | string | number | 192 |
| interval | 自动轮播间隔时间，单位为 ms | number | 5000 |
| list | 轮播数据列表，支持字符串数组或对象数组 | string[] | SwiperItem[] | [] |
| loop | 是否循环播放 | boolean | true |
| video-loop | 视频是否循环播放 | boolean | true |
| muted | 视频是否静音播放 | boolean | true |
| next-margin | 后边距 | string | number | 0 |
| indicator-position | 指示器位置，可选值为 left、top-left、top、top-right、bottom-left、bottom、bottom-right、right | IndicatorPositionType | bottom |
| previous-margin | 前边距 | string | number | 0 |
| radius | 轮播圆角 | string | number | - |
| snap-to-edge | 是否将边距应用到首尾元素 | boolean | false |
| indicator | 指示器配置，传入 false 时隐藏指示器 | boolean | Partial<SwiperIndicatorProps> | true |
| image-mode | 图片裁剪模式，取值参考 uni-app Image 组件 mode | ImageMode | aspectFill |
| show-menu-by-longpress | 是否开启长按图片显示识别小程序码菜单 | boolean | false |
| value-key | 选项对象中图片地址字段名 | string | value |
| text-key | 选项对象中标题字段名 | string | text |
| autoplay-video | 视频是否自动播放 | boolean | true |
| stop-previous-video | 切换轮播项时是否停止上一个视频播放 | boolean | true |
| stop-autoplay-when-video-play | 视频播放时是否停止自动轮播 | boolean | false |
| adjust-height | 自动根据滑块高度调整容器高度，可选值为 first、current、highest、none，仅支付宝小程序支持 | AdjustHeightType | highest |
| adjust-vertical-height | vertical 为 true 时强制让 adjust-height 生效，仅支付宝小程序支持 | boolean | false |
| custom-indicator-class | 自定义指示器类名 | string | '' |
| custom-image-class | 自定义图片类名 | string | '' |
| custom-prev-image-class | 自定义前一个图片类名 | string | '' |
| custom-next-image-class | 自定义后一个图片类名 | string | '' |
| custom-item-class | 自定义轮播项类名 | string | '' |
| custom-prev-class | 自定义前一个轮播项类名 | string | '' |
| custom-next-class | 自定义后一个轮播项类名 | string | '' |
| custom-text-class | 自定义标题类名 | string | '' |
| custom-text-style | 自定义标题样式 | string | '' |
| custom-class | 根节点自定义样式类 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击轮播项时触发 | { index: number; item: SwiperItem | string } |
| change | 轮播切换时触发 | { current: number; source: string } |
| animationfinish | 轮播动画结束时触发 | { current: number; source: string } |
| update:current | 当前轮播项更新时触发 | number |

#### Slots

| name | 说明 |
| --- | --- |
| default | 自定义轮播项内容，参数为 { item, index } |
| indicator | 自定义指示器内容，参数为 { current, total } |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-swiper-border-radius | $radius-zero | 轮播容器圆角 |
| --wot-swiper-item-padding | $padding-zero | 轮播项内边距 |
| --wot-swiper-item-text-color | $text-white | 轮播文案颜色 |
| --wot-swiper-item-text-font-size | $typography-body-size-extra-large | 轮播文案字号 |
| --wot-swiper-item-text-spacing | $spacing-loose | 轮播文案定位间距 |
> 完整 5 个 CSS 变量见源码

### table

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| data | 表格数据源（必填） | Record<string, any>[] | - |
| border | 是否显示边框 | boolean | true |
| stripe | 是否显示斑马纹 | boolean | true |
| height | 表格最大高度，设置后可纵向滚动；开启虚拟滚动时必须传入数值类型 | string | number | - |
| show-header | 是否显示表头 | boolean | true |
| ellipsis | 单元格文本是否超出两行后省略 | boolean | false |
| index | 是否显示索引列，传入对象时可自定义索引列配置（prop 除外） | boolean | Omit<Partial<TableColumnProps>, 'prop'> | false |
| fixed-header | 是否固定表头（使用 CSS sticky 定位） | boolean | true |
| span-method | 合并单元格方法，回调参数为 SpanMethodParams，返回 { rowspan, colspan } 或 void（等同 { rowspan: 1, colspan: 1 }） | SpanMethod | - |
| virtual | 是否开启虚拟滚动，大数据量时只渲染可视区域行 | boolean | false |
| row-height | 虚拟滚动固定行高，开启 virtual 时必须传入 | number | 44 |
| buffer | 虚拟滚动可视区域上下各额外预渲染行数 | number | 5 |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| sort-method | 点击可排序列表头后触发 | column: TableColumn |
| row-click | 点击表格行时触发 | { rowIndex: number } |

#### Slots

| name | 说明 |
| --- | --- |
| default | 表格列内容，通常放置一个或多个 wd-table-column |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-table-bg | $filled-oppo | 表格背景色 |
| --wot-table-color | $text-main | 表格文字颜色 |
| --wot-table-stripe-bg | $filled-bottom | 斑马纹背景色 |
| --wot-table-border-color | $border-main | 边框颜色 |
| --wot-table-border-width | $stroke-main | 边框宽度 |
| --wot-table-cell-font-size | $typography-body-size-main | 单元格字号 |
| --wot-table-cell-line-height | $typography-body-line-height-size-main | 单元格行高 |
| --wot-table-cell-padding | $padding-loose $padding-extra-loose | 单元格内边距 |
| --wot-table-shadow-gradient | linear-gradient(270deg, $base-transparent 0%, $opac-2_04 100%) | 固定列阴影渐变背景 |
| --wot-table-sort-button-height | $n-20 | 排序按钮高度 |
> 完整 10 个 CSS 变量见源码

### watermark

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| content | 水印文字内容 | string | '' |
| image | 水印图片地址，支持网络图片和 Base64 图片，钉钉小程序仅支持网络图片 | string | '' |
| image-height | 图片高度 | number | 100 |
| image-width | 图片宽度 | number | 100 |
| gutter-x | X 轴间距，单位为 px | number | 0 |
| gutter-y | Y 轴间距，单位为 px | number | 0 |
| width | 单个水印画布宽度，单位为 px | number | 100 |
| height | 单个水印画布高度，单位为 px | number | 100 |
| full-screen | 是否铺满整个页面 | boolean | true |
| color | 水印文字颜色 | string | #C9CBD4 |
| size | 水印文字大小，单位为 px | number | 14 |
| font-style | 水印字体样式，仅微信、支付宝和 H5 支持，可选值为 normal、italic、oblique | WatermarkFontStyle | normal |
| font-weight | 水印字体粗细，仅微信、支付宝和 H5 支持 | string | number | normal |
| font-family | 水印字体系列，仅微信、支付宝和 H5 支持 | string | PingFang SC |
| rotate | 水印旋转角度 | number | -25 |
| z-index | 水印层级 | number | 1100 |
| opacity | 水印透明度，取值范围为 0 到 1 | number | - |
| layout | 水印布局，可选值为 grid、staggered | WatermarkLayout | grid |
| custom-style | 自定义根节点样式 | string | '' |
| custom-class | 自定义根节点样式类 | string | '' |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-watermark-opacity | $opacity-dimmer | 水印透明度 |
> 完整 1 个 CSS 变量见源码

### curtain

#### Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| v-model / modelValue | 绑定值，控制幕帘显示与关闭 | boolean | false |
| value | 绑定值（已废弃，请使用 modelValue） | boolean | false |
| src | 幕帘图片地址，必须使用网络地址 | string | - |
| width | 幕帘图片宽度，默认单位 px | number | - |
| to | 幕帘图片点击链接 | string | - |
| close-position | 关闭按钮位置，可选值为 inset、top、bottom、top-left、top-right、bottom-left、bottom-right | string | inset |
| close-on-click-modal | 点击遮罩是否关闭 | boolean | false |
| hide-when-close | 关闭时是否隐藏弹出层（display: none） | boolean | true |
| z-index | 设置层级 | number | 10 |
| custom-close-class | 关闭按钮自定义类名 | string | '' |
| custom-close-style | 关闭按钮自定义样式 | string | '' |
| root-portal | 是否从页面中脱离，用于解决 fixed 失效问题（H5: teleport，App: renderjs，小程序: root-portal） | boolean | false |
| show-menu-by-longpress | 开启长按图片显示识别小程序码菜单，仅微信小程序支持 | boolean | false |
| close-on-click | 点击图片时是否关闭幕帘 | boolean | true |
| custom-class | 根节点自定义类名 | string | '' |
| custom-style | 根节点自定义样式 | string | '' |

#### Events

| 事件名称 | 说明 | 参数 |
| --- | --- | --- |
| click | 点击幕帘图片时触发 | - |
| close | 弹出层关闭时触发 | - |
| closed | 弹出层关闭动画结束后触发 | - |
| click-modal | 点击遮罩时触发 | - |
| beforeenter | 进入前触发 | - |
| enter | 进入时触发 | - |
| afterenter | 进入后触发 | - |
| beforeleave | 离开前触发 | - |
| leave | 离开时触发 | - |
| afterleave | 离开后触发 | - |
| load | 图片加载完成时触发 | event |
| error | 图片加载失败时触发。图片加载失败后，即使 modelValue 为 true 也不会展示幕帘 | - |

#### Slots

| name | 说明 |
| --- | --- |
| close | 自定义关闭按钮 |

#### CSS 变量

| 名称 | 默认值 | 描述 |
| --- | --- | --- |
| --wot-curtain-content-radius | $radius-large | 幕帘内容圆角 |
| --wot-curtain-content-close-color | $text-white | 幕帘关闭图标颜色 |
| --wot-curtain-content-close-size | $n-32 | 幕帘关闭图标尺寸 |
| --wot-curtain-content-close-inset | $spacing-extra-loose | 幕帘关闭图标偏移 |
> 完整 4 个 CSS 变量见源码

## 组合式函数 Hooks

### use-upload
> 数据采集失败

### use-count-down
> 数据采集失败

### use-toast
> 数据采集失败

### use-dialog
> 数据采集失败

### use-image-preview
> 数据采集失败

### use-video-preview
> 数据采集失败

### use-config-provider
> 数据采集失败

---

> 知识库自动生成于 2026-07-23 | 源 JSON: wotui-data.json