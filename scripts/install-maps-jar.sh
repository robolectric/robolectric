#!/bin/sh
#
# This script installs the Google APIs maps jar in your local Maven repository.
#
# Usage:
#   install-maps-jar.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install dir.

for mapsDir in `ls -1d $ANDROID_HOME/add-ons/addon-google_apis-google-*`; do
  (
    name=`grep ^name= $mapsDir/manifest.ini | cut -d= -f2`
    api=`grep ^api= $mapsDir/manifest.ini | cut -d= -f2`
    revision=`grep ^revision= $mapsDir/manifest.ini | cut -d= -f2`

    echo "Installing $name ${api}_r${revision} from $mapsDir..."
    mvn -q install:install-file -DgroupId=com.google.android.maps -DartifactId=maps \
      -Dversion=${api}_r${revision} -Dpackaging=jar -Dfile=$mapsDir/libs/maps.jar
  )
done

echo "Done!"



