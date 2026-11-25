@echo off
call gradlew.bat assembleRelease
if errorlevel 1 goto :end
copy /Y "app\build\outputs\apk\release\app-release.apk" "app-release.apk" >nul
echo.
echo Release APK: %~dp0app-release.apk
:end
pause
