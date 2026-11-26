# PerAppVolume / 音量调节

基于 Miuix 的 LSPosed 模块，用于为不同应用设置进入时的媒体音量。

## 系统要求

- Android 13 及以上
- LSPosed / Vector，API 101 及以上
- 推荐 LSPosed 2.2.1 / API 102
- 目标应用需要具备 `MODIFY_AUDIO_SETTINGS` 权限

## 安装与使用

1. 安装 APK。
2. 在 LSPosed / Vector 中启用 **音量调节**。
3. 在作用域中勾选需要控制的应用。
4. 打开“音量调节”，进入“音量配置”。
5. 为应用设置音量；新添加的应用默认音量为 0%。
6. 强制停止目标应用后重新打开。

模块只对 LSPosed 作用域中的应用生效。

## 构建环境

- JDK 21
- Android SDK Platform 37.2
- Build-Tools 37.0.0
- Gradle 9.9.2
- AGP 9.5.2
- Kotlin 2.4.21

构建：

```bash
./gradlew assembleRelease
```

产物：

```text
app/build/outputs/apk/release/app-release.apk
```

## 参考

- Miuix: <https://github.com/compose-miuix-ui/miuix>
- LSPosed: <https://github.com/LSPosed/LSPosed>

## License

[Apache-2.0](LICENSE)
