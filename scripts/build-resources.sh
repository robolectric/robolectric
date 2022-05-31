#!/bin/bash

set -x

rootDir=$(dirname $(dirname $0))
projects=("robolectric")

for project in "${projects[@]}"
do
  androidProjDir="$rootDir/$project"
  echo $androidProjDir

  aapts=( $ANDROID_HOME/build-tools/28.0.*/aapt )
  aapt=${aapts[-1]}
  inDir=$androidProjDir/src/test/resources
  outDir=$androidProjDir/src/test/resources
  javaSrc=$androidProjDir/src/test/java

  mkdir -p $outDir
  mkdir -p $javaSrc

  $aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-28/android.jar \
    -S $inDir/res -M $inDir/AndroidManifest.xml \
    -A $inDir/assets \
    -F $outDir/resources.ap_ \
    -J $javaSrc \
    --no-version-vectors
done
