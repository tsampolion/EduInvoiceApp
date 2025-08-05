# Android SDK and JDK Setup

This document describes how to set up the Android SDK and JDK environment for the EduInvoiceApp project.

## Prerequisites

- Android Studio installed (contains JDK/JBR)
- Android SDK installed (usually located in `%LOCALAPPDATA%\Android\Sdk`)

## Environment Variables

The following environment variables need to be set:

- `ANDROID_HOME`: Points to the Android SDK location
- `ANDROID_SDK_ROOT`: Points to the Android SDK location (alternative name)
- `JAVA_HOME`: Points to the JDK/JBR location
- `PATH`: Should include Android SDK tools and JDK bin directory

## Setup Scripts

### 1. Quick Setup (Current Session Only)
Run one of these scripts to set up environment variables for the current terminal session:

**PowerShell:**
```powershell
.\setup_android_env.ps1
```

**Command Prompt:**
```cmd
setup_env.bat
```

### 2. Permanent Setup (System-wide)
To set up environment variables permanently (requires Administrator privileges):

```powershell
.\setup_permanent_env.ps1
```

## Manual Setup

If you prefer to set up environment variables manually:

### Windows Environment Variables

1. Open System Properties (Win + R, type `sysdm.cpl`)
2. Click "Environment Variables"
3. Under "System Variables" (or "User Variables"), add:

**System Variables:**
- `ANDROID_HOME`: `C:\Users\dimit\AppData\Local\Android\Sdk`
- `ANDROID_SDK_ROOT`: `C:\Users\dimit\AppData\Local\Android\Sdk`
- `JAVA_HOME`: `C:\Program Files\Android\Android Studio\jbr`

**PATH Variable:**
Add these entries to the PATH:
- `%ANDROID_HOME%\platform-tools`
- `%ANDROID_HOME%\tools`
- `%ANDROID_HOME%\tools\bin`
- `%JAVA_HOME%\bin`

## Verification

After setup, verify the installation:

```powershell
# Check Java version
java -version

# Check Android SDK
adb version

# Check Gradle
.\gradlew --version

# Test build
.\gradlew clean
```

## Project Configuration

The project is configured for:
- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 26
- **Java Version**: 17
- **Kotlin Version**: 2.1.10

## Troubleshooting

### Java not found
- Ensure `JAVA_HOME` points to the correct JDK location
- Check that `%JAVA_HOME%\bin` is in the PATH

### Android SDK not found
- Verify `ANDROID_HOME` points to the correct SDK location
- Ensure Android SDK platform-tools are installed

### Gradle build issues
- Run `.\gradlew clean` to clear build cache
- Check that all environment variables are set correctly
- Ensure you have the required SDK platforms installed

## Notes

- The JDK is provided by Android Studio (JetBrains Runtime)
- Environment variables set by scripts are session-only unless using the permanent setup script
- Restart your terminal/IDE after making permanent environment variable changes 