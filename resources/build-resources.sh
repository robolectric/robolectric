#!/bin/bash

aapt=$ANDROID_HOME/build-tools/26.0.0/aapt
raw=`pwd`/resources/src/test/resources/rawresources
binary=`pwd`/resources/src/test/resources/binaryresources
javaSrc=`pwd`/resources/src/test/java

mkdir -p $binary
mkdir -p $javaSrc

$aapt p -v -f -m --auto-add-overlay -I $ANDROID_HOME/platforms/android-25/android.jar \
  -S $raw/res -M $raw/AndroidManifest.xml \
  -F $binary/resources.ap_ \
  -J $javaSrc
