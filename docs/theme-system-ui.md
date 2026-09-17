# 系统栏与启动主题审查

## 涉及文件

- `app/src/main/java/com/peihua/touchmonitor/ui/ServiceApp.kt`
- `app/src/main/java/com/peihua/touchmonitor/activity/ActivityPage.kt`
- `app/src/main/java/com/peihua/touchmonitor/utils/ContextExt.kt`
- `app/src/main/res/values/themes.xml`
- `app/src/main/AndroidManifest.xml`

## 发现

### 导航栏颜色与图标策略冲突（P0）

`ServiceApp.kt` 固定调用 `setNavigationBarColor(Color.Black)`，同时 `themes.xml` 将 `windowLightNavigationBar` 设为 `true`。浅色主题下，导航栏背景、图标亮度和内容主题可能不一致。

### 系统栏配置分散（P0）

- `ServiceApp.kt` 根据主题调用 `enableEdgeToEdge`。
- `ActivityPage.kt` 又在 Activity 创建时调用默认 `enableEdgeToEdge()`。
- XML Splash 主题继续声明导航栏颜色和图标模式。

三个入口相互覆盖，维护时难以预测最终系统栏状态。

### 在组合期间执行系统副作用（P1）

`ServiceApp.kt` 在 `AppTheme` content 内直接调用 `enableEdgeToEdge`。该调用应放到 `SideEffect` 中，以明确它是根据 Compose 状态更新的系统副作用。

### Splash 与运行时主题割裂（P1）

Splash 固定白色，`postSplashScreenTheme` 为透明主题。深色主题启动可能出现白色闪屏；透明运行时窗口也不适合作为常规 Compose 应用基础主题。

## 目标实现

建立唯一的系统栏应用点：

```kotlin
@Composable
fun ApplySystemBars(isDark: Boolean) {
    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()) { isDark },
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()) { isDark },
        )
    }
}
```

实际 API 参数可依据当前 AndroidX 版本调整；关键约束是：

1. 不再固定黑色导航栏。
2. 图标亮暗只由当前有效的 `isDark` 决定。
3. Activity 不再重复配置系统栏。
4. XML 只保留 Splash 所必需的启动期属性。

## XML 主题调整

- 新增非透明 `Theme.App` 作为 `postSplashScreenTheme`。
- Splash 保持品牌白色是可接受的产品选择；若要避免深色主题白闪，增加 `values-night/themes.xml` 的深色 Splash 背景与适配 Logo。
- 普通 Activity 使用 `Theme.App`；仅冷启动入口使用 `SplashScreen`。
