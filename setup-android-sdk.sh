#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for ChatGPT Codex & CI runners
#
# v3: Hardened against persistent `ca-certificates-java` errors by purging
#     and reinstalling the package to fix broken base-image states.
###############################################################################
set -euo pipefail

# ---- 0. Pre-flight Checks & Environment Setup -------------------------------
if [[ "$EUID" -ne 0 ]] || ! command -v apt-get >/dev/null 2>&1; then
  echo "ERROR: This script must be run as root and requires 'apt-get'." >&2
  exit 1
fi
export DEBIAN_FRONTEND=noninteractive

# ---- Tunables ---------------------------------------------------------------
ANDROID_SDK_ROOT="${HOME}/android-sdk"
CMDLINE_VERSION="11076708"
API_LEVEL="35"
BUILD_TOOLS="35.0.0"
NDK_VERSION="27.0.11718014"
CMAKE_VERSION=""
# -----------------------------------------------------------------------------


###############################################################################
# PHASE 1: AGGRESSIVE SYSTEM SANITIZATION
# This is the critical fix for broken base images in sandboxed environments.
###############################################################################
echo ">>>> 1 · Refreshing apt index and updating sources"
apt-get update -y

echo ">>>> 2 · Purging and reinstalling 'ca-certificates-java' to fix broken state"
# The --purge flag is essential. It removes the package and its config files.
apt-get remove --purge -y ca-certificates-java
# Ensure the problematic directory is gone before we proceed.
rm -rf /etc/ssl/certs/java
# Now, install the essential packages. Reinstalling ca-certificates-java
# alongside the JDK in a clean state should resolve the dependency error.
apt-get install -y --no-install-recommends \
  ca-certificates-java \
  openjdk-21-jdk \
  curl \
  unzip \
  git \
  build-essential \
  libglu1-mesa

# As a final measure, force-configure anything that might be pending.
dpkg --configure -a
apt-get -f install -y


###############################################################################
# PHASE 2: Download and Install Android SDK
###############################################################################
echo ">>>> 3 · Fetching Android cmdline-tools r${CMDLINE_VERSION}"
mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
cd /tmp
# Use --fail to ensure HTTP errors (like 404) cause the script to abort
curl -fsSLo cmdline-tools.zip \
  "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
unzip -q cmdline-tools.zip
mv cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest"

# Flatten the directory structure if using an older zip format
if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools" ]; then
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"/* \
     "${ANDROID_SDK_ROOT}/cmdline-tools/latest/"
  rmdir "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"
fi
cd -


###############################################################################
# PHASE 3: Environment Configuration
###############################################################################
echo ">>>> 4 · Exporting ANDROID_* vars"
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

echo ">>>>    Java version check"
java -version


###############################################################################
# PHASE 4: Android Component Installation
###############################################################################
run_sdk() {
  set +e
  yes | "$@"
  local yes_rc=${PIPESTATUS[0]:-0}
  local cmd_rc=${PIPESTATUS[1]:-${PIPESTATUS[0]:-1}}
  set -e
  if [[ $cmd_rc -eq 0 && ( $yes_rc -eq 0 || $yes_rc -eq 141 ) ]]; then
    return 0
  fi
  return "$cmd_rc"
}

echo ">>>> 5 · Installing Android SDK components"
if [[ -z "$CMAKE_VERSION" ]]; then
  CMAKE_VERSION=$(sdkmanager --list --channel=0 | awk -F'|' '/cmake;[0-9]/ {gsub(/^[ \t]+|[ \t]+$/, "", $1); sub(/^cmake;/,"",$1); print $1}' | sort -V | tail -n1)
fi
echo ">>>>    Using CMake $CMAKE_VERSION"

run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" \
  "platform-tools" "platforms;android-${API_LEVEL}" "build-tools;${BUILD_TOOLS}" \
  "cmake;${CMAKE_VERSION}" "ndk;${NDK_VERSION}" \
  "extras;android;m2repository" "extras;google;m2repository"

echo ">>>> 6 · Accepting SDK licences"
run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses


###############################################################################
# PHASE 5: Finalization
###############################################################################
echo ">>>> 7 · Writing local.properties for Gradle"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${CODING_PROJECT_ROOT:-$PWD}/local.properties"

echo ">>>> 8 · Cleaning up"
apt-get clean
rm -rf /var/lib/apt/lists/*
rm -f /tmp/cmdline-tools.zip

echo ">>>> Android SDK bootstrap complete!"
