# 主题改造实施计划

## 目标与边界

### 目标

- 浅色、深色、跟随系统和动态色行为一致。
- 通用 UI 统一使用 Material 3 `ColorScheme`，不再依赖系统深色状态或硬编码黑白。
- LED、二维码、画板、量角器、死点检测等工具的内容色保持稳定或允许用户配置。
- 大屏通过布局响应式适配，而不是放大全局字体和圆角。

### 默认决策

- 动态色支持 Light、Dark、System 三种模式。
- 首页多色图标保留为功能识别色，但不用于通用容器和文字。
- 本次只剥离主题层对 `values-sw*dp` 的依赖，不删除现有尺寸资源。
- 增加夜间 Splash，减少深色模式白闪。

## 阶段一：主题状态与系统栏（P0）

### 1. 统一主题模式与动态色

**文件：** `app/src/main/java/com/peihua/touchmonitor/ui/theme/Theme.kt`

- 在 `AppTheme` 中计算唯一有效的 `isDark`：
  - `System`：读取系统深色状态。
  - `Light`：固定 `false`。
  - `Dark`：固定 `true`。
- 对所有主题模式传入 `config.dynamicColor`。
- Android 12 以下自动回退至静态 `lightScheme` / `darkScheme`。
- 向下游传递 `isDark`，而不是用 `ThemeMode` 推导实际明暗状态。

**验收：** Android 12+ 的固定浅色和固定深色模式，开启动态色后均使用壁纸色系，同时不改变各自明暗模式。

### 2. 集中管理系统栏

**文件：**

- `app/src/main/java/com/peihua/touchmonitor/ui/ServiceApp.kt`
- `app/src/main/java/com/peihua/touchmonitor/activity/ActivityPage.kt`
- `app/src/main/java/com/peihua/touchmonitor/utils/ContextExt.kt`

**修改：**

- 在 `ServiceApp` 新增唯一的 `ApplySystemBars(isDark)`。
- 通过 `SideEffect` 统一调用 `enableEdgeToEdge`，同时决定状态栏和导航栏背景及图标亮暗。
- 删除固定黑色导航栏设置。
- 删除 `HomeScreenActivity` 和 `AutoScrollScreenActivity` 中重复的 `enableEdgeToEdge()`。
- 旧 `autoSystemBarStyle` 无其他调用时删除，避免保留第二套策略。

**验收：** 浅色、深色、动态色下状态栏和导航栏图标均有足够对比度；手势和三键导航均正常。

### 3. 修复启动主题

**文件：**

- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-night/themes.xml`（新增）
- `app/src/main/AndroidManifest.xml`

**修改：**

- 新增非透明 `Theme.App` 作为运行时主题。
- `SplashScreen.postSplashScreenTheme` 指向 `Theme.App`，不再使用透明运行时主题。
- `MainActivity` 保留 Splash；普通 Activity 使用 `Theme.App`。
- 新增夜间 Splash 背景与适配图标样式。
- `FileViewerActivity` 仅在确有透明窗口需求时保留 `Theme.Transparent`。

**验收：** 冷启动、后台恢复和深色主题启动时，无透明窗口闪烁或明显白闪。

### 4. 优先迁移影响全局一致性的页面

**文件：**

- `ui/screen/main/FunctionScreen.kt`
- `ui/screen/function/appmanager/AppDetailScreen.kt`
- `ui/screen/dialog/Dialog.kt`
- `ui/screen/function/devices/DeviceInfoScreen.kt`
- `ui/components/AppTopBar.kt`

**修改：**

- 移除 `FunctionScreen` 的 `isSystemInDarkTheme()` 和 `Colors` 调用。
- 固定白色 Card、Dialog 和信息容器替换为 `surfaceContainerLow` / `surfaceContainerHigh`。
- 主文字使用 `onSurface`，辅助文字和图标使用 `onSurfaceVariant`，边框使用 `outlineVariant`。
- TopBar 显式使用 `MaterialTheme.colorScheme.surface`。

## 阶段二：主题令牌与工具内容颜色（P1）

### 1. 建立工具颜色令牌

**新增文件：** `app/src/main/java/com/peihua/touchmonitor/ui/theme/ToolColors.kt`

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

- 在 `AppTheme` 提供 `LocalToolColors`。
- 工具页面的内容层读取工具令牌。
- 用户保存的颜色配置优先于工具默认值。
- Toolbar、按钮、表单、菜单与弹窗仍使用 `MaterialTheme`。

### 2. 迁移工具页面的控制层

**涉及文件：**

- `ui/screen/function/LedScreen.kt`
- `ui/screen/function/images/QrCodeGeneratorScreen.kt`
- `ui/screen/function/SimplePaintScreen.kt`
- `ui/screen/function/AngleMeterScreen.kt`
- `ui/components/CycleRulerView.kt`
- `ui/screen/function/devices/ScreenDeadPixelsScreen.kt`
- 媒体播放相关 Screen

**保留内容色：**

| 功能 | 默认内容色 |
|---|---|
| LED | 黑底白字，可由用户修改 |
| 二维码 | 黑白，可由用户修改 |
| 画板 | 白画布、黑画笔、白色橡皮擦 |
| 死点检测 | 红、绿、蓝、黑、白纯色 |
| 量角器 | 黑底、蓝刻度 |
| 播放器 | 黑色媒体背景、半透明遮罩、白色控制图标 |

## 阶段三：排版、形状与大屏（P1）

### 1. 主题尺寸脱离 sw 线性缩放

**文件：**

- `ui/theme/Type.kt`
- `ui/theme/Shape.kt`
- `ui/components/text/ScaleText.kt`
- `ui/components/AppTopBar.kt`

**修改：**

- Typography 使用固定 Material 3 语义字号和行高。
- Shapes 使用固定 `4dp / 8dp / 12dp / 16dp` 等语义圆角。
- 移除主题和通用组件对 `dimensionSpResource`、`dimensionResource(dp_*)` 的依赖。
- 既有 `values-sw*dp/dimens.xml` 暂不删除；仅保留给画布和需要随屏幕尺寸变化的工具页面。

### 2. 大屏布局响应式改造

- 以窗口尺寸类别作为断点依据。
- 中屏和大屏使用内容最大宽度、双栏布局和更多网格列数。
- 不再通过增加全局字体、圆角、按钮高度实现大屏适配。

## 阶段四：通用 UI 批量迁移与清理（P1）

### 迁移顺序

1. 首页、功能页、设置页、导航、TopBar。
2. Card、列表、下拉菜单、Dialog、空状态和错误状态。
3. 应用管理、下载、媒体等业务页的非内容层。
4. 工具页的 Toolbar、控制条和设置弹窗。

### 清理项

- 将 `Colors.kt` 的最后调用迁移到 `ColorScheme` 或 `ToolColors` 后删除。
- `AppColor.kt` 仅保留首页功能识别色，改为明确命名。
- 删除或正式接入未使用的 `LocalTintTheme`。
- 中/高对比度色板若不提供无障碍设置入口则删除；若保留必须增加配置与测试。

## 验收矩阵

| 维度 | 必测项 |
|---|---|
| 主题模式 | Light / Dark / System |
| 动态色 | Android 12+ 开关；固定浅色与固定深色均测试 |
| 系统栏 | 状态栏与导航栏图标对比度；全屏页进入和退出后的恢复 |
| 启动 | 冷启动、后台恢复、深色 Splash 过渡 |
| 工具 | LED、二维码、画板、死点检测、量角器、媒体播放内容色 |
| 设备 | 小屏、平板、横屏、分屏 |
| 无障碍 | 字体 100% 与 130%，禁用态与辅助文本对比度 |

## 构建与自动化验证

- 新增主题状态解析的单元测试。
- 增加 Compose 烟测，覆盖 Function、Settings、Dialog、AppDetail。
- 最终执行：
  - `./gradlew testDebugUnitTest`
  - `./gradlew connectedDebugAndroidTest`
  - `./gradlew assembleDebug`

## 本次不处理的内容

- 不强制改变工具画布和媒体内容颜色。
- 不删除用户已经可配置的颜色。
- 不全面废弃 `values-sw*dp/dimens.xml`；该工作应在后续按页面分批完成。
