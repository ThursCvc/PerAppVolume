@echo off
adb devices
echo.
echo If your phone is shown as "device" above, press any key to install the release APK.
pause >nul
adb install -r "app\build\outputs\apk\release\app-release.apk"
pause
