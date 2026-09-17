# 主题改造实现记录

## 背景与问题根因

项目此前的主题实现存在四类相互影响的问题：

1. **主题状态不唯一。** 固定浅色和深色模式没有传递动态色配置；`FunctionScreen` 直接读取系统深色模式，因此会与用户在应用内选择的主题冲突。
2. **系统栏配置分散。** 运行时固定导航栏为黑色、Compose 内直接调用 `enableEdgeToEdge`、Activity 中重复调用 Edge-to-edge、XML Splash 中又固定导航栏属性，最终样式取决于调用顺序。
3. **主题令牌依赖 `values-sw*dp`。** 字体和默认文字尺寸使用按最小宽度线性缩放的资源。例如 `sp_14` 在 `sw1080dp` 会放大至 `42sp`，圆角和间距也会随之放大，导致大屏文字、圆角和触控尺寸失衡。
4. **通用 UI 直接使用固定白色和旧色板。** 深色、动态色下，卡片和 Dialog 会保持白色，页面无法遵从 Material 3 色表。

## 本次实现

### 主题状态与动态色

**文件：** `ui/theme/Theme.kt`

- `AppTheme` 统一计算有效 `isDark`：System 读取系统状态、Light 固定浅色、Dark 固定深色。
- 三种主题模式均传递 `dynamicColor`；Android 12 以下自动使用静态浅/深色方案。
- Theme content 回调传递有效 `isDark`，避免下游重新从 `ThemeMode` 或系统状态推导。

### 固定 Material 排版与工具颜色边界

**文件：**

- `ui/theme/Type.kt`
- `ui/theme/ToolColors.kt`
- `ui/components/text/ScaleText.kt`

- 新增 `AppTypography`，全部使用固定 Material 3 `sp` 尺寸与行高，不读取 `values-sw*dp`。
- `ScaleText` 的无样式后备字号固定为 `14.sp`；正常情况下尊重 `MaterialTheme.typography`。
- 新增 `ToolColors` / `LocalToolColors`，为 LED、量角器和媒体等工具内容色提供独立令牌。当前工具页维持既有内容视觉；后续迁移时可直接接入，不会把画布色混入应用主题色。

### 系统栏与运行时主题

**文件：**

- `ui/ServiceApp.kt`
- `activity/ActivityPage.kt`
- `res/values/themes.xml`
- `res/values-night/themes.xml`
- `AndroidManifest.xml`

- 新增 `ApplySystemBars(isDark)`，使用 `SideEffect` 和 AndroidX `SystemBarStyle.auto` 统一应用透明状态栏、导航栏以及图标亮暗。
- 删除固定黑色导航栏与 accompanist 系统栏控制器。
- 删除两个辅助 Activity 的重复 `enableEdgeToEdge()`。
- 新增非透明 `Theme.App` 作为 Compose 运行时窗口主题。
- Splash 结束后进入 `Theme.App`，不再进入透明窗口主题。
- 新增夜间资源目录：深色模式启动时使用 `#121317`，避免白色窗口过渡。
- 应用与普通 Activity 继承 `Theme.App`；仅 `MainActivity` 保留 `SplashScreen`。
- `FileViewerActivity` 保持透明主题，因为它承担外部文件打开入口，其窗口行为未在本次主题改造中改变。

### 通用界面颜色迁移

**文件：**

- `ui/screen/main/FunctionScreen.kt`
- `ui/screen/function/appmanager/AppDetailScreen.kt`
- `ui/screen/function/devices/DeviceInfoScreen.kt`
- `ui/screen/dialog/Dialog.kt`
- `ui/components/AppTopBar.kt`

- 功能页移除 `isSystemInDarkTheme()` 与旧 `Colors` 调色板；页面、标签和分类图标映射至 `surfaceContainerLow`、`secondaryContainer`、`primary`、`secondary`、`tertiary` 等 Material 3 语义色。
- 应用详情的三组卡片使用 `surfaceContainerLow`；普通操作使用 `primary` / `secondary`，卸载操作使用 `error`。
- 设备信息和通用 Dialog / 进度 Dialog 使用 `surfaceContainerHigh`，不再固定白色。
- AppTopBar 明确使用 `surface`、`onSurface` 与 `primary`；标题、图标、阴影和圆角不再读取 `values-sw*dp`。

### 响应式适配方式

**文件：** `ui/components/AdaptiveContent.kt`

新增 `AdaptiveContent`：

- 手机和窄窗口保持全宽。
- 宽屏窗口将主内容限制为最大 `840.dp`，水平居中。
- 不根据屏幕宽度放大字体、圆角或间距。

`FunctionScreen` 已接入该容器，并将其自身的通用间距、圆角、图标和最小行高改为固定 dp。该策略优先保证阅读宽度和信息密度，适合后续复用于设置、应用详情等表单型页面。

## 为什么不直接删除 values-sw*dp

项目中仍有大量画布、仪表、媒体和旧页面引用 `values-sw*dp`。一次性删除会改变工具页面的可视尺寸和触控区域，风险高。

本次采取渐进迁移：

1. 主题层、Typography、通用文字、TopBar 和功能页不再引用该资源体系。
2. 新增页面和迁移中的通用页面使用固定语义尺寸与 `AdaptiveContent`。
3. 工具画布保留按可用空间计算的逻辑；后续按页面将旧布局迁移到 Compose 约束、`widthIn`、网格列数或双栏布局。

## 验证结果

- `./gradlew :app:compileDebugKotlin`：通过。
- `ANDROID_SERIAL=0123456789ABCDEF ./gradlew :app:installDebug -q`：安装成功。
- 真机 `AILABS_FG01`（横向大窗口）验证功能页：
  - 深色主题下卡片、标签、TopBar 和侧边导航颜色一致。
  - 功能主内容居中且限制在可读宽度内。
  - 字体、图标与圆角没有随大窗口宽度线性放大。

构建仍会报告项目既有警告：重复 `WRITE_SETTINGS` 权限、Room DAO nullable Collection、`FlowRowOverflow` 废弃、`Build.SERIAL` 废弃；它们与本次主题改造无关。

## 后续迁移建议

1. 将 `AdaptiveContent` 接入 Settings、AppDetail、下载列表等表单/列表页。
2. 逐页迁移 `Colors.kt` 和 `AppColor.kt` 的通用 UI 用色；首页功能识别色保留为专用令牌。
3. 工具页控制层接入 `MaterialTheme` 与 `LocalToolColors`，内容画布保持固定语义色或用户配置。
4. 为 Light / Dark / System / 动态色增加 Compose UI 烟测；增加冷启动、手势导航、三键导航和字体 130% 的回归测试。

## 工具页面颜色迁移（第二轮）

### 迁移原则

工具页面分为两层：

- **内容层：** 二维码码体、LED 字幕、画板、量角器刻度、死点检测纯色和媒体画面。这些颜色承担扫描、测量、测试或任意画面对比度职责，不应被浅色、深色或动态主题替换。
- **控制层：** 表单描边、辅助文字、按钮、菜单和叠层控制器。这些元素应使用 `MaterialTheme.colorScheme`，以适配应用主题。

### 本轮实现

**文件：**

- `ui/theme/ToolColors.kt`
- `ui/screen/function/LedScreen.kt`
- `ui/screen/function/images/QrCodeGeneratorScreen.kt`
- `ui/screen/function/SimplePaintScreen.kt`
- `ui/screen/function/AngleMeterScreen.kt`
- `ui/screen/function/video/VideoPlayerScreen.kt`
- `ui/screen/function/audio/AudioPlayerScreen.kt`

**ToolColors 扩展：**

- LED：黑色默认背景、白色默认文字。
- 二维码：黑色前景、白色背景。
- 量角器：蓝色刻度。
- 媒体：半透明黑色控制遮罩。

`LedScreen` 和二维码生成页继续使用 `rememberColorSaveable`。工具令牌仅提供首次显示的默认值，用户使用颜色选择器后的状态仍优先。

**控制层迁移：**

- 二维码生成页的 Logo 路径提示改为 `onSurfaceVariant`，所有配置项描边改为 `outline`。
- 画板橡皮擦宽度预览改为 `outline`；白色画布、黑色默认画笔和白色橡皮擦保持固定。
- 量角器相机开关改为 `surfaceContainerHigh`、`primary` 和 `onSurface`；黑色测量舞台与透明相机预览叠层保持不变。
- 视频与音频播放器的返回按钮遮罩改为 `LocalToolColors.mediaScrim`；图标仍固定白色，保证覆盖任意媒体帧时可读。

**明确未迁移：**

- 死点检测的红、绿、蓝、黑、白全屏测试色。
- 时间屏幕的黑底白字。
- `CycleRulerView` 与 `RulerView` 的画布默认色；量角器调用方仅显式传入 ToolColors 刻度色。
- QR、LED、画板的用户可配置内容色。

### 验证结果

- `./gradlew :app:compileDebugKotlin`：通过。
- 真机打开二维码生成页：深色主题下配置容器使用主题描边与辅助文本；前景色块为黑色、背景色块为白色，二维码默认可扫描对比度未改变。

## 第三批：共享组件与功能页控制层迁移

### 共享组件

**`ui/components/ModifierExt.kt`**

- 四个 `Modifier.surface()` 重载从普通 `fun` 改为 `@Composable fun`，默认 `backgroundColor` 从 `Color.White` 改为 `MaterialTheme.colorScheme.surface`。所有调用方（`FileViewerActivity`、`AppTopBar`、`Settings`）均已在 Composable 上下文中，无需额外改动。
- `topFadeMask` 和 `bottomFadeMask`（各两个重载）的默认渐变端点色从 `Color.White` 改为 `MaterialTheme.colorScheme.surface`，避免深色模式下遮罩出现白色渐变。

**`ui/theme/Theme.kt` + `ui/components/scrollbar/Scrollbar.android.kt`**

- 在 `Theme` 内通过 `CompositionLocalProvider` 提供 `LocalScrollbarStyle`，使用 `colorScheme.outlineVariant`（未悬停）和 `colorScheme.outline`（悬停）替代原先硬编码的 `Color(0x1a1F1F1F)` / `Color(0x808F8F8F)`。滚动条颜色现在随主题自动切换。

**`ui/components/SliderTips.kt`**

- 滑块气泡提示文字色从 `Color.White` 改为 `MaterialTheme.colorScheme.onPrimary`（背景为 `primary`）。

**`ui/components/shape/ArrowDownShape.kt`**

- `BubbledText` 文字色从 `Color.White` 改为 `MaterialTheme.colorScheme.onPrimary`（背景为 `primary`）。移除了不再使用的 `Color` import。

### 功能页

**`ui/screen/main/AccountScreen.kt`**

- 头像描边从 `Color.White` 改为 `colorScheme.onPrimaryContainer`（头像位于 `primaryContainer` 背景上）。

**`ui/screen/function/DayNewsScreen.kt`**

- 标题蓝色从硬编码 `color_ff5187f4` 改为 `colorScheme.primary`。
- 日期、副标题、新闻条目文字色从 `Colors.Grey[700]` 改为 `colorScheme.onSurfaceVariant`。
- 移除了 `Colors` 和 `color_ff5187f4` 的 import。

**`ui/screen/storage/StorageScreen.kt`**

- 文件列表头部行背景从 `Colors.Grey[100]` 改为 `MaterialTheme.colorScheme.surfaceContainerHigh`。
- 移除了 `Colors` import。

### 明确未迁移

- `HomeScreen.kt` 的 `AppColor.*` 图标背景色：各功能分类的标识色，非主题色。
- `HomeScreen.kt` 的 `Color.White` 图标 tint：在彩色背景上需保持白色以确保对比度。
- `HorizonScreen.kt` 的 `LevelColor` / `TiltColor`：水平仪内容色，绿色=水平、红色=倾斜为语义色。
- `CycleRulerView` 的默认参数色：工具内容色，调用方按需传入。

### 验证结果

- `./gradlew :app:compileDebugKotlin`：通过，无新增错误。
