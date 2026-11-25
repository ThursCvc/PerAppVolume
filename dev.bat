@echo off
call gradlew.bat installDebug
if errorlevel 1 goto :end
adb logcat -c
adb logcat -s PerAppVolume
:end
pause
