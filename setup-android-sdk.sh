#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for CI/CD runners
#
# v5: Definitive fix.
#     - Resolves 'ca-certificates-java' crash by separating JDK installation
#       from certificate installation, ensuring correct directory state.
#     - Hardened for user context, file ownership, and CI reproducibility.
###############################################################################
set -euo pipefail

# ---- 0. Pre-flight Checks & Environment Setup -------------------------------
if [[ "$EUID" -ne 0 ]] || ! command -v apt-get >/dev/null 2>&1; then
  echo "ERROR: This script must be run as root (e.g., via sudo) and requires 'apt-get'." >&2
  exit 1
fi

TARGET_USER="${SUDO_USER:-ubuntu}"
TARGET_HOME=$(eval echo "~$TARGET_USER")

if ! id -u "$TARGET_USER" >/dev/null 2>&1; then
    echo "ERROR: The target user '$TARGET_USER' does not exist. Cannot proceed." >&2
    exit 1
fi

export DEBIAN_FRONTEND=noninteractive

# ---- Tunables ---------------------------------------------------------------
ANDROID_SDK_ROOT="${TARGET_HOME}/android-sdk"
CMDLINE_VERSION="11076708"
API_LEVEL="35"
BUILD_TOOLS="35.0.0"
NDK_VERSION="27.0.11718014"
CMAKE_VERSION="3.22.1" # Hardcoded for reproducibility
# -----------------------------------------------------------------------------


###############################################################################
# PHASE 1: SYSTEM SANITIZATION & DEPENDENCY INSTALLATION
###############################################################################
echo ">>>> 1 · Refreshing apt index"
apt-get update -y

echo ">>>> 2 · Fixing Java environment"
# DEFINITIVE FIX: Perform the purge, JDK install, and certificate install
# in separate, sequential steps to guarantee correct state.

echo ">>>>    Step 2a: Purging potentially broken java certificate state"
apt-get remove --purge -y ca-certificates-java
rm -rf /etc/ssl/certs/java

echo ">>>>    Step 2b: Installing the JDK first to create the correct directory structure"
apt-get install -y --no-install-recommends openjdk-21-jdk

echo ">>>>    Step 2c: Re-installing ca-certificates-java now that the JRE exists"
apt-get install -y --no-install-recommends ca-certificates-java

echo ">>>>    Step 2d: Installing remaining system dependencies"
apt-get install -y --no-install-recommends \
  curl \
  unzip \
  git \
  sudo \
  build-essential \
  libglu1-mesa

dpkg --configure -a
apt-get -f install -y


###############################################################################
# PHASE 2: Download and Install Android SDK (as Target User)
###############################################################################
echo ">>>> 3 · Fetching Android cmdline-tools (as user: $TARGET_USER)"
sudo -u "$TARGET_USER" bash << EOF
set -euo pipefail
mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
cd /tmp
curl -fsSLo cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
unzip -q -d "${ANDROID_SDK_ROOT}/cmdline-tools" cmdline-tools.zip
mv "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
EOF


###############################################################################
# PHASE 3: Environment Configuration
###############################################################################
echo ">>>> 4 · Exporting ANDROID_* vars system-wide"
cat > /etc/profile.d/android-sdk.sh <<EOF
export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT}
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export PATH=\$PATH:\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools
EOF
chmod +x /etc/profile.d/android-sdk.sh

# shellcheck source=/dev/null
source /etc/profile.d/android-sdk.sh


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

echo ">>>> 5 · Installing Android SDK components (using CMake ${CMAKE_VERSION})"
sudo -u "$TARGET_USER" run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" \
  "platform-tools" "platforms;android-${API_LEVEL}" "build-tools;${BUILD_TOOLS}" \
  "cmake;${CMAKE_VERSION}" "ndk;${NDK_VERSION}" \
  "extras;android;m2repository" "extras;google;m2repository"

echo ">>>> 6 · Accepting SDK licences"
sudo -u "$TARGET_USER" run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses


###############################################################################
# PHASE 5: Finalization
###############################################################################
echo ">>>> 7 · Writing local.properties for Gradle"
PROJECT_DIR="${CODING_PROJECT_ROOT:-$PWD}"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${PROJECT_DIR}/local.properties"
chown "${TARGET_USER}:${TARGET_USER}" "${PROJECT_DIR}/local.properties"

echo ">>>> 8 · Cleaning up"
apt-get clean > /dev/null
rm -rf /var/lib/apt/lists/*
rm -f /tmp/cmdline-tools.zip

echo ">>>> Android SDK bootstrap complete for user '$TARGET_USER'!"
