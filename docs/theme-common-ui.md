# 通用界面主题迁移

## 审查结论

项目存在约 516 处 `Color(...)`、`Color.White`、`Color.Black` 等直接用色，分布于约 35 个 Kotlin 文件；`MaterialTheme.colorScheme` 调用约 78 处。通用页面在固定深色主题时容易保留白底或按系统深色模式错误渲染。

## 优先修复页面

### 功能页

`ui/screen/main/FunctionScreen.kt` 直接调用 `isSystemInDarkTheme()` 并使用 `Colors.Grey/Red`。用户选择固定浅色或深色主题时，它不会遵从应用实际主题。

迁移为 `MaterialTheme.colorScheme`：

- 文本：`onSurface` / `onSurfaceVariant`
- 标签背景：`secondaryContainer`
- 页面/卡片：`background` / `surfaceContainerLow`
- 图标：`primary` 或按功能语义从 `ToolColorTokens` 获取

### 卡片、对话框与信息页

以下位置存在固定 `Color.White`：

- `ui/screen/function/appmanager/AppDetailScreen.kt`
- `ui/screen/dialog/Dialog.kt`
- `ui/screen/function/devices/DeviceInfoScreen.kt`

将卡片容器替换为 `surfaceContainerLow` 或 `surfaceContainer`，文本替换为 `onSurface`，描边替换为 `outlineVariant`。

### 首页

`ui/screen/main/HomeScreen.kt` 使用 `AppColor` 的多色图标背景。首页可保留“功能识别色”，但这些颜色不应承担卡片或文字对比度职责；卡片容器与文字仍使用 Material 颜色。

## 通用组件迁移规范

| 组件 | 容器 | 主文字 | 辅助文字/图标 | 选中态 |
|---|---|---|---|---|
| Scaffold 页面 | `background` | `onBackground` | `onSurfaceVariant` | - |
| Card / List item | `surfaceContainerLow` | `onSurface` | `onSurfaceVariant` | `secondaryContainer` |
| Dialog / Menu | `surfaceContainerHigh` | `onSurface` | `onSurfaceVariant` | `secondaryContainer` |
| 主按钮 | `primary` | `onPrimary` | - | - |
| Outlined Button | `surface` | `primary` | - | `primaryContainer` |
| 分隔线 / 边框 | `outlineVariant` | - | - | `outline` |

## 迁移策略

1. 优先替换页面壳层、Card、Dialog、TopBar、Bottom Navigation 的硬编码色。
2. 再替换普通文本、图标、Divider、禁用态。
3. 不改工具内容颜色；详见 `theme-tool-pages.md`。
4. 每完成一个页面，分别验证浅色、深色、动态色。
