#!/bin/sh
#
# This script installs the Android Support v4 jar in your local Maven repository.
#
# Usage:
#   install-support-jar.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.
#  3. You have installed the Android Support (compatibility) libraries from the SDK installer.

echo "Installing com.android.support:support-v4 from $ANDROID_HOME/extras"
mvn -q install:install-file -DgroupId=com.android.support -DartifactId=support-v4 \
  -Dversion=19.0.0 -Dpackaging=jar -Dfile="$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar"

echo "Done!"
