#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for CI/CD runners
#
# v8: Definitive Cache-Aware Version.
#     - Combines the robust multi-step apt installation with the cache check.
#     - This correctly handles the initial 'cache miss' run and provides
#       maximum speed on subsequent 'cache hit' runs.
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

# This check prevents the main installation on a cache hit
if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/latest" ]; then
    echo ">>>> Android SDK found in cache. Skipping installation."
else
    echo ">>>> Android SDK not found in cache. Starting fresh installation..."

    # --- Phase 1: Full System Installation on Cache Miss ---
    echo ">>>> 1 · Refreshing apt index"
    apt-get update -y

    echo ">>>> 2 · Fixing Java environment and installing dependencies"
    # DEFINITIVE FIX: Use the proven, sequential installation method.
    echo ">>>>    Step 2a: Purging potentially broken java certificate state"
    # Use '|| true' to prevent failure if the package isn't installed
    apt-get remove --purge -y ca-certificates-java || true
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

    # --- Phase 2: SDK Installation on Cache Miss ---
    sudo -u "$TARGET_USER" bash << 'EOF'
    set -euo pipefail

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
      set +e; yes | "$@" >/dev/null; local rc=${PIPESTATUS[1]}; set -e
      if [[ $rc -ne 0 ]]; then echo "SDK manager command failed with exit code $rc."; exit $rc; fi
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
fi

###############################################################################
# These phases always run to configure the environment for the build steps.
###############################################################################
echo ">>>> Configuring environment for current build job"
# PHASE 3: Environment Configuration (For future shells)
cat > /etc/profile.d/android-sdk.sh <<EOF
export ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT}
export ANDROID_HOME=${ANDROID_SDK_ROOT}
export PATH=\$PATH:\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools
EOF
chmod +x /etc/profile.d/android-sdk.sh

# PHASE 5: Finalization
PROJECT_DIR="${CODING_PROJECT_ROOT:-$PWD}"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${PROJECT_DIR}/local.properties"
chown "${TARGET_USER}:${TARGET_USER}" "${PROJECT_DIR}/local.properties"

echo ">>>> Android SDK bootstrap complete!"
