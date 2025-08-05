# Android SDK and JDK Environment Setup Script
# This script sets up the necessary environment variables for Android development

Write-Host "Setting up Android SDK and JDK environment variables..." -ForegroundColor Green

# Set Android SDK environment variables
$env:ANDROID_HOME = "C:\Users\dimit\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\dimit\AppData\Local\Android\Sdk"

# Set JDK environment variables
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Add Android SDK tools to PATH
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
$env:PATH += ";$env:ANDROID_HOME\tools"
$env:PATH += ";$env:ANDROID_HOME\tools\bin"
$env:PATH += ";$env:JAVA_HOME\bin"

# Display the set environment variables
Write-Host "`nEnvironment variables set:" -ForegroundColor Yellow
Write-Host "ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Cyan
Write-Host "ANDROID_SDK_ROOT: $env:ANDROID_SDK_ROOT" -ForegroundColor Cyan
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan

# Test Java installation
Write-Host "`nTesting Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = & "$env:JAVA_HOME\bin\java.exe" -version 2>&1
    Write-Host "Java version:" -ForegroundColor Green
    Write-Host $javaVersion[0] -ForegroundColor White
} catch {
    Write-Host "Error testing Java: $_" -ForegroundColor Red
}

# Test Android SDK
Write-Host "`nTesting Android SDK..." -ForegroundColor Yellow
if (Test-Path "$env:ANDROID_HOME\platform-tools\adb.exe") {
    Write-Host "Android SDK platform-tools found" -ForegroundColor Green
} else {
    Write-Host "Android SDK platform-tools not found" -ForegroundColor Red
}

Write-Host "`nEnvironment setup complete!" -ForegroundColor Green
Write-Host "Note: These environment variables are set for the current session only." -ForegroundColor Yellow
Write-Host "To make them permanent, add them to your system environment variables." -ForegroundColor Yellow 