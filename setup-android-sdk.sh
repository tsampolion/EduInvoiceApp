#!/usr/bin/env bash
###############################################################################
# Android SDK bootstrap for ChatGPT Codex & CI runners
#
# This script is hardened against common CI/CD failures:
# - Fixes apt/dpkg state errors related to `ca-certificates-java`.
# - Is idempotent (safe to run multiple times).
# - Fails immediately on critical errors (e.g., failed downloads).
# - Auto-selects the newest stable CMake.
# - Works on Debian 12 and Ubuntu 22.04-24.04.
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
CMDLINE_VERSION="11076708"        # cmdline-tools r12b
API_LEVEL="35"                    # Android 15
BUILD_TOOLS="35.0.0"
NDK_VERSION="27.0.11718014"
CMAKE_VERSION=""                  # blank → auto-detect newest stable
# -----------------------------------------------------------------------------


###############################################################################
# PHASE 1: SYSTEM SANITIZATION
# Clean up the package manager state to prevent common CI errors.
###############################################################################
echo ">>>> 1 · Refreshing apt index and fixing initial state"
apt-get update -y
# Install apt-utils to prevent debconf warnings and use fix-broken to
# resolve any inconsistent states from the base container image.
apt-get install -y --no-install-recommends apt-utils
apt-get -f install -y

echo ">>>> 2 · (Ubuntu only) Enabling universe repository"
if grep -qi ubuntu /etc/os-release ; then
  apt-get install -y --no-install-recommends software-properties-common
  add-apt-repository -y universe
  apt-get update -y
fi


###############################################################################
# PHASE 2: TARGETED PREEMPTIVE FIX for `ca-certificates-java`
# Force configuration of pending packages and ensure directories exist.
###############################################################################
echo ">>>> 3 · Forcibly configuring any pending packages"
# This directly addresses the error where `ca-certificates-java` is
# unpacked but not configured.
dpkg --configure -a

echo ">>>> 4 · Pre-creating Java certs directory as a safeguard"
# This prevents the race condition where the `ca-certificates-java` post-install
# script runs before the OpenJDK script has created this directory.
mkdir -p /etc/ssl/certs/java


###############################################################################
# PHASE 3: CORE PACKAGE INSTALLATION
###############################################################################
echo ">>>> 5 · Choosing best OpenJDK (21 preferred, else 17)"
if apt-cache show openjdk-21-jdk >/dev/null 2>&1 ; then
  JDK_PACKAGE=openjdk-21-jdk                     # For Ubuntu with universe repo
else
  JDK_PACKAGE=openjdk-17-jdk                     # Debian 12 default; AGP 8+ requires JDK 17+
fi

echo ">>>>    Installing base packages: $JDK_PACKAGE"
# With the system state now clean, this transaction should succeed reliably.
apt-get install -y --no-install-recommends \
  "$JDK_PACKAGE" curl unzip git build-essential libglu1-mesa


###############################################################################
# 6 · Download cmdline-tools and place them in <sdk>/cmdline-tools/latest
###############################################################################
echo ">>>> 6 · Fetching Android cmdline-tools r${CMDLINE_VERSION}"
mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
cd /tmp
# Use --fail to ensure HTTP errors (like 404) cause the script to abort
curl -fsSLo cmdline-tools.zip \
  "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
unzip -q cmdline-tools.zip
mv cmdline-tools "${ANDROID_SDK_ROOT}/cmdline-tools/latest"

# Flatten the directory structure if using an older cmdline-tools zip format
if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools" ]; then
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"/* \
     "${ANDROID_SDK_ROOT}/cmdline-tools/latest/"
  rmdir "${ANDROID_SDK_ROOT}/cmdline-tools/latest/cmdline-tools"
fi
cd -


###############################################################################
# 7 · Environment variables
###############################################################################
echo ">>>> 7 · Exporting ANDROID_* vars"
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

echo ">>>>    Java version check"
java_version_output=$(java -version 2>&1 | head -n 1)
echo "$java_version_output"
java_major=$(echo "$java_version_output" | grep -oE '[0-9]+' | head -n 1)
if [ "${java_major:-0}" -lt 17 ]; then
  echo "WARNING: JDK 17+ required. Run 'source ~/.profile' or reopen the shell." >&2
fi


###############################################################################
# 8 · Helper: run sdkmanager safely, swallowing benign SIGPIPE errors
###############################################################################
run_sdk() {
  set +e  # Temporarily disable "exit on error"
  yes | "$@"
  local yes_rc=${PIPESTATUS[0]:-0}
  local cmd_rc=${PIPESTATUS[1]:-${PIPESTATUS[0]:-1}}
  set -e
  # Treat success + SIGPIPE 141 as overall success
  if [[ $cmd_rc -eq 0 && ( $yes_rc -eq 0 || $yes_rc -eq 141 ) ]]; then
    return 0
  fi
  return "$cmd_rc"
}


###############################################################################
# 9 · Install Android SDK components
###############################################################################
echo ">>>> 9 · Determining newest stable CMake"
if [[ -z "$CMAKE_VERSION" ]]; then
  CMAKE_VERSION=$(sdkmanager --list --channel=0 | \
      awk -F'|' '/cmake;[0-9]+\.[0-9]+\.[0-9]+/ \
        {gsub(/^[ \t]+|[ \t]+$/, "", $1); sub(/^cmake;/,"",$1); print $1}' | \
      sort -V | tail -n1)
fi
echo ">>>>    Using CMake $CMAKE_VERSION"

echo ">>>>    Installing platform ${API_LEVEL}, build-tools ${BUILD_TOOLS}, NDK ${NDK_VERSION}"
run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" \
  "platform-tools" \
  "platforms;android-${API_LEVEL}" \
  "build-tools;${BUILD_TOOLS}" \
  "cmake;${CMAKE_VERSION}" \
  "ndk;${NDK_VERSION}" \
  "extras;android;m2repository" \
  "extras;google;m2repository"

echo ">>>> 10 · Accepting SDK licences"
run_sdk sdkmanager --sdk_root="${ANDROID_SDK_ROOT}" --licenses


###############################################################################
# 10 · Gradle fallback (local.properties)
###############################################################################
echo ">>>> 11 · Writing local.properties"
echo "sdk.dir=${ANDROID_SDK_ROOT}" > "${CODING_PROJECT_ROOT:-$PWD}/local.properties"


###############################################################################
# 11 · Clean-up
###############################################################################
echo ">>>> 12 · Cleaning up"
apt-get clean
rm -rf /var/lib/apt/lists/*
rm -f /tmp/cmdline-tools.zip

echo ">>>> Android SDK bootstrap complete!"
