# 主题基础设施审查

## 当前结构

- Material 3 主题入口：`app/src/main/java/com/peihua/touchmonitor/ui/theme/Theme.kt`
- 静态浅/深色方案：`Color.kt`
- 用户配置：`ThemeMode.kt`、`Models.kt`、`SystemSettingsStore.kt`
- 字体与形状：`Type.kt`、`Shape.kt`
- 动态色设置页面：`ui/screen/settings/Settings.kt`

## 发现

### 动态色在固定主题下不生效（P0）

`Theme.kt` 中 `AppTheme` 仅在 `ThemeMode.System` 时传递 `config.dynamicColor`。设置页却在 Android 12+ 下对全部主题模式展示动态色开关，因此用户选择固定浅色或深色后，开关配置会保存但没有视觉效果。

## 改造方案

推荐让动态色独立于浅色、深色和跟随系统：

```kotlin
val isDark = when (config.theme) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}
Theme(darkTheme = isDark, dynamicColor = config.dynamicColor) { ... }
```

若产品要求动态色只能跟随系统，则应在固定浅/深色模式中禁用开关并给出说明，避免“可设置但不生效”。

### 形状和排版错误依赖 sw 尺寸资源（P0）

`Shape.kt` 与 `Type.kt` 使用 `dp_*`、`sp_*` 资源。项目的 `values-sw*dp/dimens.xml` 对这些资源做线性缩放，例如：

| token | sw360 | sw720 | sw1080 |
|---|---:|---:|---:|
| `dp_16` | 16dp | 32dp | 48dp |
| `sp_14` | 14sp | 28sp | 42sp |

这会让全局圆角、字体、行高在平板与桌面模式过度膨胀，且字体还会叠加系统 font scale。

## 目标令牌体系

### Material 3 令牌

保留 `Color.kt` 作为唯一静态 `ColorScheme` 来源。通用 UI 仅使用：

- `background` / `onBackground`：页面
- `surfaceContainer*` / `onSurface`：卡片、菜单、弹窗
- `primary` / `onPrimary`：主操作
- `secondaryContainer` / `onSecondaryContainer`：选择态、次级操作
- `onSurfaceVariant`：弱文本和辅助图标
- `outlineVariant`：分隔线和描边
- `error` / `errorContainer`：错误状态

### 固定主题尺寸

主题的 Shapes 与 Typography 应使用固定语义值，而非 `sw` 资源：

```kotlin
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
```

排版以 Material 3 基准字号定义。大屏适配使用布局断点、最大内容宽度和网格列数，不通过等比放大所有字体与圆角。

## 清理项

- `Theme.kt` 的中/高对比度色板目前未被选择；要么实现无障碍对比度配置，要么删除。
- `Tint.kt` 中 `LocalTintTheme` 未被 Provider 注入；要么正式接入，要么删除。
- `Colors.kt` 和 `AppColor.kt` 不应再作为通用主题色来源；保留的业务专属颜色应迁移至工具令牌模块。
