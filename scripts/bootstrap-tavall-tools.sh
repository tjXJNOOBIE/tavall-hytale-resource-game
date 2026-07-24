#!/usr/bin/env bash
set -euo pipefail

workspace="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
version_init="$workspace/gradle/tavall-tools-version.init.gradle"
checkout="$(mktemp -d)"
trap 'rm -rf "$checkout"' EXIT

modules=(
  "tavall-logging"
  "tavall-concurrency"
  "tavall-reflection"
  "tavall-di"
  "tavall-eventbus"
  "tavall-cache"
  "tavall-database"
  "tavall-registry"
  "tavall-scheduler"
)

for module in "${modules[@]}"; do
  module_checkout="$checkout/$module"
  git clone --quiet --depth 1 --branch working/migrate-to-gradle \
    "https://github.com/tjXJNOOBIE/$module.git" "$module_checkout"
  "$module_checkout/gradlew" -p "$module_checkout" --no-daemon --max-workers=1 \
    -I "$version_init" -x test publishToMavenLocal
done
