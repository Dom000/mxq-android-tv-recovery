#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
JDK="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home}"
mkdir -p build/tests
"$JDK/bin/javac" -d build/tests src/dev/mxq/home/AppOrder.java tests/AppOrderTest.java \
  src/dev/mxq/home/OnlineWallpaperPolicy.java tests/OnlineWallpaperPolicyTest.java
"$JDK/bin/java" -cp build/tests dev.mxq.home.AppOrderTest
"$JDK/bin/java" -cp build/tests dev.mxq.home.OnlineWallpaperPolicyTest
python3 tests/check_contract.py
