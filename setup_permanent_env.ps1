# Permanent Android SDK and JDK Environment Setup Script
# This script sets up the environment variables permanently in the Windows registry
# Run this script as Administrator for system-wide changes

Write-Host "Setting up permanent Android SDK and JDK environment variables..." -ForegroundColor Green
Write-Host "Note: This script modifies system environment variables." -ForegroundColor Yellow

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

if (-not $isAdmin) {
    Write-Host "Warning: This script should be run as Administrator for system-wide changes." -ForegroundColor Red
    Write-Host "You can still set user-specific environment variables." -ForegroundColor Yellow
    $scope = "User"
} else {
    $scope = "Machine"
}

# Define environment variables
$envVars = @{
    "ANDROID_HOME" = "C:\Users\dimit\AppData\Local\Android\Sdk"
    "ANDROID_SDK_ROOT" = "C:\Users\dimit\AppData\Local\Android\Sdk"
    "JAVA_HOME" = "C:\Program Files\Android\Android Studio\jbr"
}

# Set environment variables
foreach ($var in $envVars.GetEnumerator()) {
    try {
        [Environment]::SetEnvironmentVariable($var.Key, $var.Value, $scope)
        Write-Host "Set $($var.Key) = $($var.Value)" -ForegroundColor Green
    } catch {
        Write-Host "Error setting $($var.Key): $_" -ForegroundColor Red
    }
}

# Update PATH for current session
$env:ANDROID_HOME = $envVars["ANDROID_HOME"]
$env:ANDROID_SDK_ROOT = $envVars["ANDROID_SDK_ROOT"]
$env:JAVA_HOME = $envVars["JAVA_HOME"]

# Add Android SDK tools to PATH
$newPathEntries = @(
    "$env:ANDROID_HOME\platform-tools",
    "$env:ANDROID_HOME\tools",
    "$env:ANDROID_HOME\tools\bin",
    "$env:JAVA_HOME\bin"
)

# Get current PATH
$currentPath = [Environment]::GetEnvironmentVariable("PATH", $scope)

# Add new entries if they don't exist
foreach ($entry in $newPathEntries) {
    if ($currentPath -notlike "*$entry*") {
        $currentPath += ";$entry"
        Write-Host "Added to PATH: $entry" -ForegroundColor Green
    }
}

# Update PATH in registry
try {
    [Environment]::SetEnvironmentVariable("PATH", $currentPath, $scope)
    Write-Host "Updated PATH environment variable" -ForegroundColor Green
} catch {
    Write-Host "Error updating PATH: $_" -ForegroundColor Red
}

Write-Host "`nPermanent environment setup complete!" -ForegroundColor Green
Write-Host "Scope: $scope" -ForegroundColor Cyan
Write-Host "You may need to restart your terminal or computer for changes to take effect." -ForegroundColor Yellow 