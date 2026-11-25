# 音量调节 / MiuixVolumeLsp

一个基于 **Miuix** 的 LSPosed 模块，用于给不同的应用设置不同的媒体音量。

A Miuix-based LSPosed module for per-app media volume control.

## 功能

- 首页显示 LSPosed 激活状态、设备信息、模块信息
- 配置页列出当前 LSPosed 作用域内的应用
- 每个应用单独设置进入时的媒体音量（0%–100%）
- 支持玻璃底栏 / 普通底栏
- 支持隐藏桌面图标，隐藏后仍可从 LSPosed 管理器进入
- LSPosed API 102 热重载（`autoHotReload=true`）
- Release 版经过 R8 裁剪，APK 约 2.3 MB

## 下载

从 GitHub Releases 下载最新 APK：

[Download latest release](https://github.com/ThursCvc/MiuixVolumeLsp/releases/latest)

要求：

- Android 13+（模块自身 `minSdk 33`）
- LSPosed / Vector，支持 API 101+
- 推荐 LSPosed 2.2.1 / API 102
- 目标应用需要具备 `MODIFY_AUDIO_SETTINGS` 权限，否则系统会拒绝修改音量

## 使用

1. 安装 APK。
2. 在 LSPosed / Vector 中启用 **音量调节**。
3. 在作用域里勾选目标应用。
4. 打开“音量调节”，进入“配置”页。
5. 拖动目标应用的滑块，设置进入该应用时的音量。
6. 强制停止目标应用，再重新打开。

模块只对 LSPosed 作用域里勾选的应用生效。不要把作用域勾成“系统框架”，除非你明确知道自己在做什么。

## 隐藏桌面图标

在“设置”页打开“隐藏桌面图标”。

模块同时声明了 `CATEGORY_INFO` 入口，LSPosed 管理器通过 `getLaunchIntentForPackage()` 可以继续打开模块设置页。

如果桌面图标隐藏后确实无法进入，可以临时用：

```bash
adb shell am start -n com.example.miuixvolume/.MainActivity
```

## 构建

环境：

- JDK 21
- Android SDK Platform 37.2
- Build-Tools 37.0.0
- Gradle Wrapper 9.9.2
- AGP 9.5.2
- Kotlin 2.4.21

Windows：

```bat
gradlew.bat assembleRelease
```

Linux / macOS：

```bash
./gradlew assembleRelease
```

产物：

```text
app/build/outputs/apk/release/app-release.apk
```

Debug 版：

```bash
./gradlew assembleDebug
```

### 本地 SDK 配置

复制 `local.properties.example` 为 `local.properties`，写入：

```properties
sdk.dir=C\:\\path\\to\\Android\\Sdk
```

或设置环境变量 `ANDROID_HOME`。

## 开发

### UI

用 Android Studio 打开项目，使用 Compose Preview 或 **Apply Changes**，不需要每次完整安装 APK。

### Hook / 逻辑

模块已实现 libxposed API 102 的 `onHotReloading` / `onHotReloaded`，并开启：

```properties
autoHotReload=true
```

安装新 APK 后，LSPosed 会把新代码热重载到运行中的目标进程，通常不需要强制停止目标应用或重启手机。

## 项目结构

```text
app/src/main/java/com/example/miuixvolume/
├── App.kt              # XposedService / RemotePreferences
├── ConfigPage.kt       # 应用列表与每个应用的音量滑块
├── HomePage.kt         # 激活状态、设备信息、模块信息
├── MainActivity.kt     # 页面骨架、底栏、主题
├── ModuleMain.kt       # Xposed 入口与 Hook 逻辑
├── PrefsKeys.kt        # 配置 key 与本地 UI 偏好
└── SettingsPage.kt     # 玻璃底栏、隐藏桌面图标
```

Xposed 元数据：

```text
app/src/main/resources/META-INF/xposed/
├── java_init.list
├── module.prop
└── scope.list
```

## 已知限制

- 音量修改发生在目标应用进程内，受目标应用权限限制。
- 如果目标应用没有 `MODIFY_AUDIO_SETTINGS`，日志会出现 `failed to change volume`。
- 隐藏桌面图标依赖 LSPosed 管理器的 `CATEGORY_INFO` 打开逻辑；不同管理器版本可能行为不同。
- Release 版经过 R8 裁剪；如果遇到运行时异常，请使用 Debug 版并提交日志。

## License

[Apache-2.0](LICENSE)

Miuix 也是 Apache-2.0 开源项目：<https://github.com/compose-miuix-ui/miuix>
