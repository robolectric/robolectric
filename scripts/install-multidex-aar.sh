#!/bin/sh
#
# This script extracts the Android Support multidex aar and installs the contained jar in your local Maven repository.
#
# Usage:
#   install-multidex-aar.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.
#  3. You have installed the Android Support (compatibility) libraries from the SDK installer.

version=1.0.0
aarLocation="$ANDROID_HOME/extras/android/m2repository/com/android/support/multidex/$version/multidex-$version.aar"
if [ ! -f "$aarLocation" ]; then
  echo "multidex $version artifact not found!"
  exit 1
fi

if [ -d "$TMPDIR" ]; then
    tmpDir=$TMPDIR
elif [ -d "$TEMP" ]; then
    tmpDir=$TEMP
else
    tmpDir=.
fi

jarLocation=$tmpDir/classes.jar
unzip -o $aarLocation classes.jar -d $tmpDir

echo "Installing com.android.support:multidex $version from $jarLocation"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=multidex \
  -Dversion=$version -Dpackaging=jar -Dfile="$jarLocation"

rm $jarLocation

echo "Done!"
