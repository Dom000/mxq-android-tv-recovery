#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
TOOLS="$SDK/build-tools/${MXQ_BUILD_TOOLS:-35.0.0}"
PLATFORM="$SDK/platforms/${MXQ_COMPILE_SDK:-android-35}/android.jar"
JDK="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home}"
export JAVA_HOME="$JDK"
export PATH="$JDK/bin:$PATH"
for tool in aapt d8 zipalign apksigner; do
  test -x "$TOOLS/$tool" || { echo "Missing SDK tool: $TOOLS/$tool" >&2; exit 1; }
done
test -f "$PLATFORM" || { echo "Missing Android platform: $PLATFORM" >&2; exit 1; }
mkdir -p build/classes build/dex .keys
python3 tools/sync_wallpapers.py
chmod 700 .keys
# Only our generated output is removed; the local signing key is preserved.
find build/classes build/dex -type f -delete
find src -name '*.java' | sort > build/sources.txt
"$JDK/bin/javac" -source 8 -target 8 -Xlint:-options -bootclasspath "$PLATFORM:$TOOLS/core-lambda-stubs.jar" \
  -d build/classes @build/sources.txt
find build/classes -name '*.class' | sort > build/classes.txt
"$TOOLS/d8" --min-api 25 --lib "$PLATFORM" --output build/dex @build/classes.txt
"$TOOLS/aapt" package -f -M AndroidManifest.xml -S res -A assets -I "$PLATFORM" -F build/resources.apk
cp build/resources.apk build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$TOOLS/zipalign" -f 4 build/unsigned.apk build/aligned.apk
if ! test -f .keys/preview.jks; then
  "$JDK/bin/keytool" -genkeypair -noprompt -keystore .keys/preview.jks \
    -storepass android -keypass android -alias evening-preview -keyalg RSA \
    -keysize 2048 -validity 10000 -dname 'CN=Evening Home Local Preview'
  chmod 600 .keys/preview.jks
fi
"$TOOLS/apksigner" sign --ks .keys/preview.jks --ks-key-alias evening-preview \
  --ks-pass pass:android --key-pass pass:android --min-sdk-version 25 \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out build/EveningHome-preview.apk build/aligned.apk
"$TOOLS/apksigner" verify --verbose --print-certs build/EveningHome-preview.apk > build/signature.txt
"$TOOLS/aapt" dump badging build/EveningHome-preview.apk > build/package.txt
"$TOOLS/zipalign" -c 4 build/EveningHome-preview.apk
shasum -a 256 build/EveningHome-preview.apk > build/EveningHome-preview.sha256
echo 'Built and verified: launcher/build/EveningHome-preview.apk'
cat build/EveningHome-preview.sha256
