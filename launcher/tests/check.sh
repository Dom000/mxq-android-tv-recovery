#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
JDK="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home}"
mkdir -p build/tests
"$JDK/bin/javac" -d build/tests src/dev/mxq/home/AppOrder.java tests/AppOrderTest.java \
  src/dev/mxq/home/OnlineWallpaperPolicy.java tests/OnlineWallpaperPolicyTest.java \
  src/dev/mxq/home/RecentsTree.java tests/RecentsTreeTest.java \
  src/dev/mxq/home/TaskIconIndex.java tests/TaskIconIndexTest.java
"$JDK/bin/java" -cp build/tests dev.mxq.home.AppOrderTest
"$JDK/bin/java" -cp build/tests dev.mxq.home.OnlineWallpaperPolicyTest
"$JDK/bin/java" -cp build/tests dev.mxq.home.RecentsTreeTest
"$JDK/bin/java" -cp build/tests dev.mxq.home.TaskIconIndexTest
python3 tests/check_contract.py
