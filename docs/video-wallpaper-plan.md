# 桌面视频壁纸（DesktopVideoScreen）实现记录

## 需求

在「功能 → 桌面视频壁纸」页面选择一个视频，将其设为桌面动态壁纸（Live Wallpaper）：视频在桌面图标后方循环播放。

> 已确认决策：视频来源用系统文件选择器（OpenDocument + 持久 URI 授权）；声音默认静音，页面提供开关、热生效。

## 现状盘点

- 入口/路由已就绪：`AppRouter.DesktopVideoScreen`、`FunctionScreen` 菜单项、中英文字符串 `text_desktop_video_wallpaper` 均存在。
- `DesktopVideoScreen.kt` 是空壳（仅 Toolbar + 空 Column）。
- Manifest 无任何 WallpaperService。
- 可复用设施：
  - media3 ExoPlayer 1.8.0（`VideoPlayerScreen` 已用）。
  - Gson 文件 DataStore 基建（`data/DataStore.kt` + `SystemSettingsStore` 模式）。
  - `WallpaperManager` 为平台 API，无需新依赖。

## 方案

### 1. VideoWallpaperService（新建 `service/VideoWallpaperService.kt`）

- `WallpaperService` + `Engine`，用 ExoPlayer 把视频渲染到 Engine 的 `SurfaceHolder` 表面：
  - `REPEAT_MODE_ONE` 无缝循环。
  - `setVideoSurfaceHolder(holder)`，`onSurfaceDestroyed` 释放 player 与 surface。
  - `onVisibilityChanged`：不可见暂停、可见恢复，省电。
- 配置读取：仿 `SystemSettingsStore` 新建 `VideoWallpaperStore`（DataStore），字段：
  - `videoUri: String`（持久化的 content URI）
  - `muted: Boolean`（默认 true）
- Engine 启动时异步读取配置流，拿到 URI 后起播；配置变化时热更新（换视频/切静音无需重新应用壁纸）。

### 2. 清单与资源

- Manifest 注册：
  ```xml
  <service
      android:name=".service.VideoWallpaperService"
      android:exported="true"
      android:label="@string/text_desktop_video_wallpaper"
      android:permission="android.permission.BIND_WALLPAPER">
      <intent-filter>
          <action android:name="android.service.wallpaper.WallpaperService" />
      </intent-filter>
      <meta-data
          android:name="android.service.wallpaper"
          android:resource="@xml/video_wallpaper" />
  </service>
  ```
- `res/xml/video_wallpaper.xml`：`<wallpaper android:thumbnail="@mipmap/ic_launcher" />`（选择器缩略图暂用应用图标）。

### 3. DesktopVideoScreen UI

- **选择视频**：`rememberLauncherForActivityResult(OpenDocument)`，mime `video/*`；拿到 URI 后 `takePersistableUriPermission`（保证重启后服务仍可读，无需运行时存储权限）。
- **预览**：`AndroidView` + ExoPlayer，循环+静音预览，`DisposableEffect` 释放；显示文件名。
- **选项**：静音开关（写入 VideoWallpaperStore，服务热更新）。
- **设为桌面壁纸**：`WallpaperManager` 发 `ACTION_CHANGE_LIVE_WALLPAPER` + `EXTRA_LIVE_WALLPAPER_COMPONENT`，由系统弹确认框；无壁纸组件环境时回退 `ACTION_LIVE_WALLPAPER_CHOOSER`。
- **状态展示**：`WallpaperManager.wallpaperInfo` 判断当前壁纸是否为本服务，是则提示"当前已启用"。

### 4. 验证

- `compileDebugKotlin` + `installDebug`：通过（exit 0）。修复一处编译错误：`WallpaperInfo` 取服务组件的属性是 `component` 而非 `service`。
- 真机（0123456789ABCDEF）全流程：
  - 选视频：`wallpaper_sample.mp4` 文件名正确显示，预览彩条动画正常。
  - 设为壁纸：按钮拉起系统 `LiveWallpaperChange` 预览页（标题"桌面视频壁纸"+「设置壁纸」确认），确认后 `dumpsys wallpaper` 显示 `mWallpaperComponent=...VideoWallpaperService`。
  - 桌面：视频在图标后方循环播放，跨视频时长（6s）取三帧画面均不同，`REPEAT_MODE_ONE` 循环有效。
  - 页面「当前已启用桌面视频壁纸」状态提示正确。

### 边界说明

- 重启持久性依赖 `takePersistableUriPermission`（标准 API，未重启验证）。
- 静音热更新走配置流收集，样本视频无音轨，声音路径未真机验证。
- 换视频/切静音通过 `configFlow.collect` 热生效，无需重新应用壁纸。

## 边界与取舍

- 不复制视频文件到私有目录（URI 持久授权已够，避免大文件拷贝耗时）；若用户删除源视频，服务静默失败显示黑屏，可接受。
- 壁纸作用域为系统 Live Wallpaper（桌面/锁屏由系统壁纸应用决定），不做自绘锁屏。
- 缩略图暂用应用图标，后续可换成所选视频首帧（需生成静态图资源，首版不做）。
