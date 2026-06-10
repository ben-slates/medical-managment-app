#!/usr/bin/env bash
set -euo pipefail

GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
NC="\033[0m"

log() { printf "${BLUE}==>${NC} %s\n" "$1"; }
ok() { printf "${GREEN}✓${NC} %s\n" "$1"; }
warn() { printf "${YELLOW}!${NC} %s\n" "$1"; }
fail() { printf "${RED}x${NC} %s\n" "$1" >&2; exit "${2:-1}"; }

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

detect_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    echo "$JAVA_HOME"
    return
  fi
  local java_bin
  java_bin="$(command -v java || true)"
  [[ -n "$java_bin" ]] || fail "Java was not found. Install OpenJDK 21." 10
  readlink -f "$java_bin" | sed 's#/bin/java##'
}

detect_android_home() {
  if [[ -n "${ANDROID_HOME:-}" && -d "$ANDROID_HOME/platforms" ]]; then
    echo "$ANDROID_HOME"
    return
  fi
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "$ANDROID_SDK_ROOT/platforms" ]]; then
    echo "$ANDROID_SDK_ROOT"
    return
  fi
  for dir in "$HOME/Android/Sdk" "$HOME/android-sdk" "/opt/android-sdk" "/usr/lib/android-sdk"; do
    [[ -d "$dir/platforms" ]] && echo "$dir" && return
  done
  fail "Android SDK was not found. Set ANDROID_HOME to your SDK path." 11
}

JAVA_HOME="$(detect_java_home)"
export JAVA_HOME
JAVA_VERSION="$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}')"
[[ "$JAVA_VERSION" == 21* ]] || fail "JDK 21 is required. Detected Java $JAVA_VERSION at $JAVA_HOME." 12
ok "Using JDK $JAVA_VERSION"

ANDROID_HOME="$(detect_android_home)"
export ANDROID_HOME
[[ -d "$ANDROID_HOME/platforms/android-35" ]] || fail "Android SDK platform 35 is missing at $ANDROID_HOME/platforms/android-35." 13
ok "Using Android SDK at $ANDROID_HOME with platform android-35"

GRADLE="./gradlew"
GRADLE_ARGS=(--no-daemon)
[[ -x "$GRADLE" ]] || fail "gradlew is not executable." 14

COMMAND="${1:-debug}"
case "$COMMAND" in
  debug|"")
    log "Building debug APK"
    "$GRADLE" "${GRADLE_ARGS[@]}" assembleDebug
    ok "Debug APK: app/build/outputs/apk/debug/app-debug.apk"
    ;;
  --release|release)
    log "Building release APK"
    "$GRADLE" "${GRADLE_ARGS[@]}" assembleRelease
    ok "Release APK: app/build/outputs/apk/release/app-release.apk"
    ;;
  clean)
    log "Cleaning project"
    "$GRADLE" "${GRADLE_ARGS[@]}" clean
    ok "Clean complete"
    ;;
  test)
    log "Running tests"
    "$GRADLE" "${GRADLE_ARGS[@]}" test
    ok "Tests complete"
    ;;
  *)
    fail "Unknown command: $COMMAND. Use ./build.sh, ./build.sh --release, ./build.sh clean, or ./build.sh test." 15
    ;;
esac
