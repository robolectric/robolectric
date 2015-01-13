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
archive="$ANDROID_HOME/extras/android/m2repository/com/android/support/multidex/$version/multidex-$version.aar"
if [ ! -f "$archive" ]; then
  echo "multidex $version artifact not found! Make sure that \$ANDROID_HOME is \
set, and that the Android Support Repository is up to date in the SDK manager."
  exit 1
fi

classes=/tmp/classes.jar
cd /tmp; jar xvf $archive

echo "Installing com.android.support:multidex $version from $archive"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=multidex -Dversion=$version -Dpackaging=jar -Dfile="$classes"

echo "Done!"
