@echo off
REM Quick Android SDK and JDK Environment Setup
REM This batch file sets up environment variables for the current session

echo Setting up Android SDK and JDK environment variables...

REM Set Android SDK environment variables
set ANDROID_HOME=C:\Users\dimit\AppData\Local\Android\Sdk
set ANDROID_SDK_ROOT=C:\Users\dimit\AppData\Local\Android\Sdk

REM Set JDK environment variables
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

REM Add Android SDK tools to PATH
set PATH=%PATH%;%ANDROID_HOME%\platform-tools
set PATH=%PATH%;%ANDROID_HOME%\tools
set PATH=%PATH%;%ANDROID_HOME%\tools\bin
set PATH=%PATH%;%JAVA_HOME%\bin

echo Environment variables set:
echo ANDROID_HOME=%ANDROID_HOME%
echo ANDROID_SDK_ROOT=%ANDROID_SDK_ROOT%
echo JAVA_HOME=%JAVA_HOME%

echo.
echo Testing Java installation...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Testing Android SDK...
if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    echo Android SDK platform-tools found
) else (
    echo Android SDK platform-tools not found
)

echo.
echo Environment setup complete!
echo Note: These environment variables are set for the current session only.
pause 