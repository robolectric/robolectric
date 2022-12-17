#!/bin/bash

set -x

# Exit the script if ANDROID_HOME is unset
set -u

rootDir=$(dirname $(dirname $0))
projects=("robolectric" "nativeruntime")

for project in "${projects[@]}"
do
  androidProjDir="$rootDir/$project"
  echo $androidProjDir

  aapts=( $ANDROID_HOME/build-tools/*/aapt )
  aapt=${aapts[-1]}
  inDir=$androidProjDir/src/test/resources
  outDir=$androidProjDir/src/test/resources
  javaSrc=$androidProjDir/src/test/java

  mkdir -p $inDir/assets
  mkdir -p $inDir/res
  mkdir -p $outDir
  mkdir -p $javaSrc

  $aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-28/android.jar \
    -S $inDir/res -M $inDir/AndroidManifest.xml \
    -A $inDir/assets \
    -F $outDir/resources.ap_ \
    -J $javaSrc \
    --no-version-vectors
done
