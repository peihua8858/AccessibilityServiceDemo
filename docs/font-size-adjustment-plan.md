# 系统字体大小调节（SystemFontSizeAdjustmentScreen）实现记录

## 需求

在功能页提供"系统字体大小调节"入口，修改**系统全局**字体缩放（所有应用生效），采用与系统设置一致的预设档位：

- 小 0.85 / 标准 1.0 / 大 1.15 / 超大 1.30
- 提供"恢复默认"（回到 1.0）
- 当前档位高亮，切换即时生效（系统广播配置变化并重建界面，本应用页面本身就是预览）

已确认决策：作用范围=系统全局；交互=滑块（初版为预设档位卡片，应用户要求改为 CustomSliderTips 滑块，steps=2 吸附到同四档标准值）。

## 现状盘点

- 屏幕 `SystemFontSizeAdjustmentScreen.kt` 已有空壳（Toolbar + 空 Column）
- 路由 `AppRouter.SystemFontSizeAdjustmentScreen`、`ServiceApp.kt:468` 导航已接好
- 功能页入口在 `FunctionScreen.kt:120`，当前被注释
- 字符串已有 `text_system_font_size_adjustment`（中/英）
- 清单已有 `WRITE_SETTINGS` 权限；`autoScroller/settings/AllSettings.kt:104` 已有 `Settings.System.canWrite` + `ACTION_MANAGE_WRITE_SETTINGS` 授权先例
- `Theme.kt:299` 使用 `baseDensity.fontScale`，系统 fontScale 变化后 LocalConfiguration 会带新值，与全局密度缩放正交组合，无需改动

## 方案

### 权限流

1. 进入页面/点击档位前检查 `Settings.System.canWrite(context)`
2. 未授权 → 跳转 `Settings.ACTION_MANAGE_WRITE_SETTINGS`，页面 onStop→onResume 之间状态由 `lifecycle` 恢复后重新检查（简化：跳转返回后 Compose 重组时重新读取 canWrite 即可）
3. 授权后写入 `Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, scale)`

### UI

- 顶部说明区：当前系统字体缩放值 + 权限状态提示（未授权时提示并附"去授权"按钮）
- 4 个档位卡片（单行/两行网格），每档显示档位名 + 缩放值 + 示例文字（按该档字号渲染直观预览）
- 当前档位高亮（primaryContainer），浮点比较用 0.01f 容差
- "恢复默认"按钮（写回 1.0）

### 技术要点

- 当前值读取：`Settings.System.getFloat(resolver, FONT_SCALE, 1f)`；页面进入时读一次存状态
- 写入异常（个别 ROM 限制）try/catch + dLog
- 写入后系统重建 Activity，返回本页时重新读取系统值刷新高亮（`remember` 初始值在重建后自然重算）
- 显示值格式：`String.format("%.2f", scale)` 或百分数（85%/100%/115%/130%）

## 验证结果（AILABS_FG01 平板 0123456789ABCDEF）

1. 功能页"系统工具"分组出现"系统字体大小调节"入口，点击进入正常渲染
2. 未授权时：红色提示卡 + "去授权"按钮；点击跳转系统设置
3. 授权返回后 ON_RESUME 自动刷新：提示卡消失、档位与恢复默认按钮可用
4. 点击"大 115%"：`settings get system font_scale` = 1.15，系统广播配置变化重建 Activity，页面文字即时放大，档位高亮切换正确
5. 点击"恢复默认"：font_scale 回 1.0，标准档重新高亮，按钮回到禁用态

## 实现变更

- `ui/screen/function/SystemFontSizeAdjustmentScreen.kt`：完整实现（权限检查/授权跳转、当前缩放读取、CustomSliderTips 滑块 steps=2 吸附 0.85/1.0/1.15/1.30、拖动中仅本地预览+松手写系统值、字号预览文本、四档刻度标签、恢复默认）
- `ui/components/SliderTips.kt`：
  - CustomSliderTips/CustomSlider 新增可选 `onValueChangeFinished`（置于 onChangValue 之前，保持尾随 lambda 调用点兼容）与 `bubbleSize` 透传
  - 修复气泡定位 bug：`range = (end-start).toInt()` 在步长 <1 的范围（如 0.85..1.30）取整为 0，比例变 NaN 把气泡甩到最右侧；改为浮点差值
- `ui/screen/main/FunctionScreen.kt`：入口取消注释
- `values/strings.xml` + `values-en-rUS/strings.xml`：新增 8 条字符串（当前缩放、四档名称、恢复默认、权限提示、去授权）
- `docs/font-size-adjustment-plan.md`：本记录

## 验证结果（补充滑块版）

- AILABS_FG01 平板（深色）：拖到 1.15/1.30 后 `settings get system font_scale` 同步且保持，气泡正确悬浮于滑块上方并显示 "115%"/"130%"，刻度标签高亮切换正确，恢复默认回 1.0 且按钮回到禁用态；130% 时功能页 chips 自动换行无裁切
- AILABS_S3_TW 1280x800（浅色）：同流程 1.15 写入生效、界面一致，恢复默认回 1.0
- 快速合成 swipe（400ms）曾在重建瞬间产生事件重放导致一次回跳（写入 1.15 后又被改回 1.0）；慢速拖动（≥1.5s）与真实手势无此现象，判定为测试工具假象，非功能缺陷

## 边界说明

- 部分定制 ROM 可能拦截第三方写 FONT_SCALE，写入失败静默记录日志并提示未生效
- 档位采用 AOSP 标准 bucket 值（0.85/1.0/1.15/1.30），连续滑块不在本功能范围
- **本测试机（AILABS 定制 ROM）行为**：`ACTION_MANAGE_WRITE_SETTINGS` 被重定向到系统"应用管理"页，无直接的"修改系统设置"开关；验证时用 `adb shell appops set com.peihua.toutchmonitor WRITE_SETTINGS allow` 授予并保留，否则此 ROM 上该功能无法使用。标准 Android 设备上会正常打开授权页

