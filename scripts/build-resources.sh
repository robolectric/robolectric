#!/bin/bash

set -x

scriptsDir=`dirname $0`
androidProjDir=`dirname $scriptsDir`/robolectric
echo $androidProjDir

aapt=$ANDROID_HOME/build-tools/26.0.1/aapt
inDir=$androidProjDir/src/test/resources
outDir=$androidProjDir/src/test/resources
javaSrc=$androidProjDir/src/test/java

mkdir -p $outDir
mkdir -p $javaSrc

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $inDir/lib1/res -M $inDir/lib1/AndroidManifest.xml \
  -J $javaSrc \
  --non-constant-id --no-version-vectors

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $inDir/lib2/res -M $inDir/lib2/AndroidManifest.xml \
  -J $javaSrc \
  --non-constant-id --no-version-vectors

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $inDir/lib3/res -M $inDir/lib3/AndroidManifest.xml \
  -J $javaSrc \
  --non-constant-id --no-version-vectors

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $inDir/lib3/res -M $inDir/lib3/AndroidManifest.xml \
  -S $inDir/lib2/res -M $inDir/lib2/AndroidManifest.xml \
  -S $inDir/lib1/res -M $inDir/lib1/AndroidManifest.xml \
  -S $inDir/res -M $inDir/AndroidManifest.xml \
  -A $inDir/assets \
  -F $outDir/resources.ap_ \
  -J $javaSrc \
  --no-version-vectors
