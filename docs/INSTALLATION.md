# Installation Guide

This guide provides step-by-step instructions for setting up EduInvoiceApp for development and production use.

## 📋 Prerequisites

### System Requirements
- **Operating System:** Windows 10/11, macOS 10.15+, or Linux (Ubuntu 18.04+)
- **Java Development Kit (JDK):** Version 17 or newer
- **Android Studio:** Version Hedgehog (2023.1.1) or newer
- **RAM:** Minimum 8GB, recommended 16GB
- **Storage:** At least 10GB free space
- **Internet Connection:** Required for initial setup and dependency downloads

### Required Software

#### 1. Java Development Kit (JDK)
Download and install JDK 17 or newer from:
- **Oracle JDK:** https://www.oracle.com/java/technologies/downloads/
- **OpenJDK:** https://adoptium.net/

Verify installation:
```bash
java -version
javac -version
```

#### 2. Android Studio
Download and install Android Studio from:
https://developer.android.com/studio

**Required Android Studio Components:**
- Android SDK Platform 35
- Android SDK Build-Tools 35.0.0
- Android SDK Command-line Tools
- Android Emulator
- Android SDK Platform-Tools

#### 3. Git
Download and install Git from:
https://git-scm.com/downloads

Verify installation:
```bash
git --version
```

## 🚀 Installation Steps

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/EduInvoiceApp.git
cd EduInvoiceApp
```

### Step 2: Environment Setup

#### Automatic Setup (Recommended)

**For Windows:**
```bash
setup_env.bat
```

**For macOS/Linux:**
```bash
bash setup-android-sdk.sh
source ~/.bashrc  # or ~/.zshrc for macOS
```

**For PowerShell:**
```powershell
.\setup_android_env.ps1
```

#### Manual Setup

If automatic setup fails, follow these manual steps:

1. **Set JAVA_HOME:**
   ```bash
   # Windows
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   
   # macOS/Linux
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
   ```

2. **Set ANDROID_HOME:**
   ```bash
   # Windows
   setx ANDROID_HOME "C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
   
   # macOS/Linux
   export ANDROID_HOME=$HOME/Library/Android/sdk
   echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.bashrc
   ```

3. **Add to PATH:**
   ```bash
   # Windows
   setx PATH "%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools"
   
   # macOS/Linux
   export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
   echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools' >> ~/.bashrc
   ```

### Step 3: Android SDK Setup

#### Using Android Studio
1. Open Android Studio
2. Go to **Tools > SDK Manager**
3. Install the following:
   - **SDK Platforms:** Android 35 (API Level 35)
   - **SDK Tools:** Android SDK Build-Tools 35.0.0
   - **SDK Tools:** Android SDK Command-line Tools
   - **SDK Tools:** Android Emulator
   - **SDK Tools:** Android SDK Platform-Tools

#### Using Command Line
```bash
# Accept licenses
yes | sdkmanager --licenses

# Install required packages
sdkmanager "platforms;android-35"
sdkmanager "build-tools;35.0.0"
sdkmanager "cmdline-tools;latest"
sdkmanager "emulator"
sdkmanager "platform-tools"
```

### Step 4: Firebase Configuration

1. **Create Firebase Project:**
   - Go to https://console.firebase.google.com/
   - Create a new project or select existing one
   - Add Android app to the project

2. **Download Configuration:**
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Set API Key:**
   Create or update `local.properties`:
   ```properties
   FIREBASE_API_KEY=your_firebase_api_key_here
   ```

### Step 5: Build Configuration

#### Gradle Properties
The project uses the following Gradle configuration:
- **Kotlin Version:** 2.1.10
- **Android Gradle Plugin:** 8.8.0
- **Gradle Version:** 8.10.2
- **Compile SDK:** 35
- **Target SDK:** 35
- **Minimum SDK:** 26

#### Local Properties
Create `local.properties` in the project root:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
FIREBASE_API_KEY=your_firebase_api_key_here
```

### Step 6: Build and Test

#### Clean Build
```bash
./gradlew clean
./gradlew assemble
```

#### Run Tests
```bash
./gradlew test
./gradlew lintDebug
```

#### Build Release
```bash
./gradlew assembleRelease
```

## 🔧 Development Setup

### IDE Configuration

#### Android Studio
1. **Import Project:**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the EduInvoiceApp directory
   - Click "OK"

2. **Configure Kotlin:**
   - Go to **File > Settings > Languages & Frameworks > Kotlin**
   - Ensure Kotlin version matches project requirements

3. **Configure Gradle:**
   - Go to **File > Settings > Build, Execution, Deployment > Gradle**
   - Set Gradle JDK to version 17

#### VS Code (Alternative)
1. Install extensions:
   - Kotlin Language
   - Android
   - Gradle for Java

2. Open the project folder
3. Configure settings for Android development

### Code Quality Tools

#### Linting
```bash
./gradlew lintDebug
```

#### Code Formatting
```bash
./gradlew ktfmtFormat
```

#### Static Analysis
```bash
./gradlew detekt
```

## 📱 Running the App

### On Emulator
1. **Create AVD:**
   - Open Android Studio
   - Go to **Tools > AVD Manager**
   - Create Virtual Device
   - Select API Level 35 device

2. **Run App:**
   ```bash
   ./gradlew installDebug
   ```

### On Physical Device
1. **Enable Developer Options:**
   - Go to **Settings > About Phone**
   - Tap "Build Number" 7 times
   - Enable "USB Debugging"

2. **Connect Device:**
   - Connect device via USB
   - Allow USB debugging when prompted

3. **Run App:**
   ```bash
   ./gradlew installDebug
   ```

## 🔒 Security Configuration

### Release Signing
For production releases, configure signing:

1. **Generate Keystore:**
   ```bash
   keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure in local.properties:**
   ```properties
   RELEASE_STORE_FILE=path/to/release.keystore
   RELEASE_STORE_PASSWORD=your_store_password
   RELEASE_KEY_ALIAS=release
   RELEASE_KEY_PASSWORD=your_key_password
   ```

### Environment Variables
Set sensitive information as environment variables:
```bash
export FIREBASE_API_KEY="your_api_key"
export RELEASE_STORE_PASSWORD="your_password"
export RELEASE_KEY_PASSWORD="your_password"
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Gradle Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew assemble

# Check Gradle version
./gradlew --version
```

#### 2. SDK Issues
```bash
# Verify SDK installation
sdkmanager --list

# Reinstall SDK components
sdkmanager --uninstall "platforms;android-35"
sdkmanager "platforms;android-35"
```

#### 3. Memory Issues
Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError
```

#### 4. Firebase Issues
- Verify `google-services.json` is in the correct location
- Check Firebase API key in `local.properties`
- Ensure Firebase project is properly configured

#### 5. Database Issues
```bash
# Clear app data (development only)
adb shell pm clear gr.eduinvoice

# Reset database (development only)
adb shell rm -rf /data/data/gr.eduinvoice/databases/
```

### Getting Help

If you encounter issues:
1. Check the [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Review the [Error Logs](../CHANGELOG.md)
3. Create an issue in the repository
4. Contact support at support@eduinvoice.com

## ✅ Verification

After installation, verify everything works:

1. **Build Success:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Tests Pass:**
   ```bash
   ./gradlew test
   ```

3. **Lint Clean:**
   ```bash
   ./gradlew lintDebug
   ```

4. **App Runs:**
   - Install and launch the app
   - Verify all screens load correctly
   - Test basic functionality

## 📚 Next Steps

After successful installation:
1. Read the [Quick Start Guide](QUICK_START.md)
2. Review the [User Manual](USER_MANUAL.md)
3. Explore the [Development Guide](DEVELOPMENT.md)
4. Check the [Testing Strategy](TESTING_STRATEGY.md)

---

*This installation guide is maintained alongside the codebase and updated with each release.*
