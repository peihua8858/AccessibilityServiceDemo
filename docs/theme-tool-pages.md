# 工具页面颜色策略

## 原则

工具页面的“内容颜色”不等于应用主题颜色。LED 字幕、二维码、死点检测、画板、量角器等页面需要稳定、可识别或可由用户配置的颜色，不能全部替换为 `MaterialTheme.colorScheme`。

## 需保留的内容色

| 页面/功能 | 示例 | 策略 |
|---|---|---|
| LED 字幕 | 黑底、白字、用户自选字色 | 保留默认黑白与用户保存配置 |
| 二维码 | 黑白码体 | 保留可扫描的默认黑白，允许用户显式自定义 |
| 死点检测 | 红、绿、蓝、黑、白 | 纯色全屏必须保持固定值 |
| 画板 | 白色画布、黑色画笔 | 作为画布默认值，不随应用深色模式变灰 |
| 量角器 | 黑底、蓝色刻度 | 保留专业工具配色；控制按钮使用主题容器色 |
| 播放器 | 黑色媒体背景、半透明遮罩 | 保留沉浸式媒体语义 |

## 推荐结构

新增独立工具令牌，例如：

```kotlin
@Immutable
data class ToolColors(
    val ledDefaultBackground: Color,
    val ledDefaultText: Color,
    val rulerTick: Color,
    val mediaScrim: Color,
)

val LocalToolColors = staticCompositionLocalOf { DefaultToolColors }
```

`AppTheme` 中提供 `LocalToolColors`。工具页从该 CompositionLocal 获取默认值，但用户在页面内选择的颜色优先级最高。

## 迁移边界

### 应使用 MaterialTheme 的部分

- 工具页 Toolbar、返回按钮、菜单、设置弹窗、底部操作栏
- 普通按钮、文本输入框、Slider、列表、权限提示

### 应使用 ToolColors 或用户配置的部分

- 工具画布
- 媒体内容层
- 全屏测试色
- 仪表/量角器/指南针刻度
- 图像处理与二维码输出内容

这样可同时保证全局浅深色一致性与工具功能语义稳定。
