#!/bin/bash

set -x

scriptsDir=`dirname $0`
androidProjDir=`dirname $scriptsDir`/robolectric
echo $androidProjDir

aapt=$ANDROID_HOME/build-tools/28.0.1/aapt
inDir=$androidProjDir/src/test/resources
outDir=$androidProjDir/src/test/resources
javaSrc=$androidProjDir/src/test/java

mkdir -p $outDir
mkdir -p $javaSrc

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $inDir/res -M $inDir/AndroidManifest.xml \
  -A $inDir/assets \
  -F $outDir/resources.ap_ \
  -J $javaSrc \
  --no-version-vectors
