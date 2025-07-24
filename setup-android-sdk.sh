      
#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for ChatGPT Codex & CI runners
# Robust against SIGPIPE 141, handles cmdline-tools ZIP layout changes,
# auto-selects the newest stable CMake, and works on Debian 12 / Ubuntu 22-24.
###############################################################################
set -euo pipefail

# ---- Pre-flight checks ------------------------------------------------------
if [[ "$EUID" -ne 0 ]] || ! command -v apt-get >/dev/null 2>&1; then
  echo "ERROR: This script must be run as root and requires 'apt-get'." >&2
  exit 1
fi
# -----------------------------------------------------------------------------

# ---- 0. Tunables ------------------------------------------------------------
ANDROID_SDK_ROOT="${HOME}/android-sdk"
CMDLINE_VERSION="11076708"        # cmdline-tools r12b
API_LEVEL="35"                    # Android 15
BUILD_TOOLS="35.0.0"
NDK_VERSION="27.0.11718014"
CMAKE_VERSION=""                  # blank → auto-detect newest stable
# -----------------------------------------------------------------------------


echo ">>>> 1 · Refreshing apt index"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y

echo ">>>> 2 · (Ubuntu only) enable universe so Java 21 is visible"
if grep -qi ubuntu /etc/os-release ; then
  apt-get install -y --no-install-recommends software-properties-common
  add-apt-repository -y universe
  apt-get update -y
fi

echo ">>>> 3 · Choosing best OpenJDK (21 preferred, else 17)"
if apt-cache show openjdk-21-jdk >/dev/null 2>&1 ; then
  JDK_PACKAGE=openjdk-21-jdk                     # Ubuntu “universe”
else
  JDK_PACKAGE=openjdk-17-jdk                     # Debian 12 default; AGP 8+ requires JDK 17+
fi

echo ">>>> 4a · Pre-creating Java certs directory to prevent race condition"
mkdir -p /etc/ssl/certs/java

echo ">>>> 4b · Installing base packages: $JDK_PACKAGE"
apt-get install -y --no-install-recommends \
  "$JDK_PACKAGE" curl unzip git build-essential libglu1-mesa


###############################################################################
# 5 · Download cmdline-tools and place them in <sdk>/cmdline-tools/latest
###############################################################################
echo ">>>> 5 · Fetching Android cmdline-tools r${CMDLINE_VERSION}"
mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
cd /tmp
# Use --fail to ensure HTTP errors cause the script to exit
curl -fsSLo cmdline-tools.zip \
  "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
unzip -q cmdline-tools.zip
mv cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest"

# Flatten only if the legacy nested folder exists (new zips don’t have it)
if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools" ]; then
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"/* \
     "${ANDROID_SDK_ROOT}/cmdline-tools/latest/"
  rmdir "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"
fi
rm cmdline-tools.zip
cd -


###############################################################################
# 6 · Environment variables
###############################################################################
echo ">>>> 6 · Exporting ANDROID_* vars"
# Only add to .profile if not already present to avoid duplication
if ! grep -q "export ANDROID_SDK_ROOT=" "$HOME/.profile"; then
  {
    echo ""
    echo "# Android SDK Environment Variables"
    echo "export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT}"
    echo "export ANDROID_HOME=${ANDROID_SDK_ROOT}"
    echo 'export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools'
  } >> "$HOME/.profile"
else
    echo ">>>>    Android environment variables already exist in ~/.profile. Skipping."
fi

export ANDROID_HOME="${ANDROID_SDK_ROOT}"
export PATH=$PATH:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

echo ">>>> 6 · Java version check"
java_version_output=$(java -version 2>&1 | head -n 1)
echo "$java_version_output"
java_major=$(echo "$java_version_output" | grep -oE '[0-9]+' | head -n 1)
if [ "${java_major:-0}" -lt 17 ]; then
  echo "WARNING: JDK 17+ required. Run 'source ~/.profile' or reopen the shell." >&2
fi


###############################################################################
# 7 · Helper: run sdkmanager safely, swallow benign SIGPIPE 141
###############################################################################
run_sdk() {
  set +e                # temporarily disable "exit on error"
  yes | "$@"
  local yes_rc=${PIPESTATUS[0]:-0}        # default 0 if index missing
  local cmd_rc=${PIPESTATUS[1]:-${PIPESTATUS[0]:-1}}  # if no [1], reuse [0]
  set -e
  # Treat success + SIGPIPE 141 as overall success
  if [[ $cmd_rc -eq 0 && ( $yes_rc -eq 0 || $yes_rc -eq 141 ) ]]; then
    return 0
  fi
  return "$cmd_rc"
}


###############################################################################
# 8 · Pick newest stable CMake (auto) and install SDK components
###############################################################################
echo ">>>> 7 · Determining newest stable CMake"
if [[ -z "$CMAKE_VERSION" ]]; then
  CMAKE_VERSION=$(sdkmanager --list --channel=0 | \
      awk -F'|' '/cmake;[0-9]+\.[0-9]+\.[0-9]+/ \
        {gsub(/^[ \t]+|[ \t]+$/, "", $1); sub(/^cmake;/,"",$1); print $1}' | \
      sort -V | tail -n1)                                # 3.22.1 today
fi
echo ">>>>    Using CMake $CMAKE_VERSION"

echo ">>>> 8 · Installing platform ${API_LEVEL}, build-tools ${BUILD_TOOLS}, NDK ${NDK_VERSION}"
run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" \
  "platform-tools" \
  "platforms;android-${API_LEVEL}" \
  "build-tools;${BUILD_TOOLS}" \
  "cmake;${CMAKE_VERSION}" \
  "ndk;${NDK_VERSION}" \
  "extras;android;m2repository" \
  "extras;google;m2repository"          # Google & Android repos

echo ">>>> 9 · Accepting SDK licences"
run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses


###############################################################################
# 9 · Gradle fallback (local.properties)
###############################################################################
echo ">>>> 10 · Writing local.properties"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${CODING_PROJECT_ROOT:-$PWD}/local.properties"


###############################################################################
# 10 · Clean-up
###############################################################################
echo ">>>> 11 · Cleaning apt cache"
apt-get clean
rm -rf /var/lib/apt/lists/*

echo ">>>> Android SDK bootstrap complete!"
