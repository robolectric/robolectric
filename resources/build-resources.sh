#!/bin/bash

resourcesProjDir=`dirname $0`
echo $resourcesProjDir

aapt=$ANDROID_HOME/build-tools/26.0.1/aapt
raw=$resourcesProjDir/src/test/resources/rawresources
binary=$resourcesProjDir/src/test/resources/binaryresources
javaSrc=$resourcesProjDir/src/test/java

mkdir -p $binary
mkdir -p $javaSrc

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $raw/res -M $raw/AndroidManifest.xml \
  -F $binary/resources.ap_ \
  -J $javaSrc
