#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for CI/CD runners
#
# v7: Optimized for CI Caching.
#     - Checks for the existence of the final SDK directory before running
#       the time-consuming installation steps. If found, skips installation.
###############################################################################
set -euo pipefail

# ---- 0. Pre-flight Checks & Environment Setup -------------------------------
if [[ "$EUID" -ne 0 ]] || ! command -v apt-get >/dev/null 2>&1; then
  echo "ERROR: This script must be run as root (e.g., via sudo) and requires 'apt-get'." >&2
  exit 1
fi

TARGET_USER="${SUDO_USER:-runner}" # GitHub Actions uses 'runner' user
TARGET_HOME=$(eval echo "~$TARGET_USER")

if ! id -u "$TARGET_USER" >/dev/null 2>&1; then
    echo "Creating missing user '$TARGET_USER'..."
    useradd --shell /bin/bash --create-home "$TARGET_USER"
fi

export DEBIAN_FRONTEND=noninteractive

# ---- Tunables ---------------------------------------------------------------
ANDROID_SDK_ROOT="${TARGET_HOME}/android-sdk"
CMDLINE_VERSION="11076708"
API_LEVEL="35"
BUILD_TOOLS="35.0.0"
NDK_VERSION="27.0.11718014"
CMAKE_VERSION="3.22.1"
# -----------------------------------------------------------------------------


###############################################################################
# PHASE 1: SYSTEM SANITIZATION & DEPENDENCY INSTALLATION
###############################################################################
echo ">>>> 1 · Refreshing apt index"
apt-get update -y

echo ">>>> 2 · Installing system dependencies"
# This will be fast if the apt cache is restored
apt-get install -y --no-install-recommends \
  openjdk-21-jdk \
  ca-certificates-java \
  curl \
  unzip \
  git \
  sudo \
  build-essential \
  libglu1-mesa

dpkg --configure -a
apt-get -f install -y


###############################################################################
# PHASE 2 & 4: SDK DOWNLOAD, INSTALL, AND CONFIG (as Target User)
# CACHING OPTIMIZATION: This entire block is skipped if the SDK is found.
###############################################################################
if [ ! -d "${ANDROID_SDK_ROOT}/cmdline-tools/latest" ]; then
    echo ">>>> Android SDK not found in cache. Starting fresh installation..."

    sudo -u "$TARGET_USER" bash << 'EOF'
    set -euo pipefail

    # Redefine variables within the sub-shell
    ANDROID_SDK_ROOT="${HOME}/android-sdk"
    API_LEVEL="35"
    BUILD_TOOLS="35.0.0"
    NDK_VERSION="27.0.11718014"
    CMAKE_VERSION="3.22.1"
    CMDLINE_VERSION="11076708"

    echo ">>>> 3 · Fetching Android cmdline-tools"
    mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
    cd /tmp
    curl -fsSLo cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
    unzip -q -d "${ANDROID_SDK_ROOT}/cmdline-tools" cmdline-tools.zip
    mv "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
    cd - > /dev/null

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

    export PATH=$PATH:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

    echo ">>>> 5 · Installing Android SDK components"
    run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" \
      "platform-tools" "platforms;android-${API_LEVEL}" "build-tools;${BUILD_TOOLS}" \
      "cmake;${CMAKE_VERSION}" "ndk;${NDK_VERSION}" \
      "extras;android;m2repository" "extras;google;m2repository"

    echo ">>>> 6 · Accepting SDK licences"
    run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses
EOF

else
    echo ">>>> Android SDK found in cache. Skipping installation."
fi


###############################################################################
# PHASE 3: Environment Configuration (For future shells)
# This always runs to ensure the environment is correctly configured.
###############################################################################
echo ">>>> 4 · Exporting ANDROID_* vars system-wide"
cat > /etc/profile.d/android-sdk.sh <<EOF
export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT}
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export PATH=\$PATH:\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools
EOF
chmod +x /etc/profile.d/android-sdk.sh


###############################################################################
# PHASE 5: Finalization
###############################################################################
echo ">>>> 7 · Writing local.properties for Gradle"
PROJECT_DIR="${CODING_PROJECT_ROOT:-$PWD}"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${PROJECT_DIR}/local.properties"
chown "${TARGET_USER}:${TARGET_USER}" "${PROJECT_DIR}/local.properties"

echo ">>>> 8 · Cleaning up temporary files"
# We do not clean apt cache here, as it's needed for caching
rm -f /tmp/cmdline-tools.zip

echo ">>>> Android SDK bootstrap complete for user '$TARGET_USER'!"
