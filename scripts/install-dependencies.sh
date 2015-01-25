#!/bin/bash
#
# This script mavenizes all dependencies from the Android SDK required to build Robolectric.
#
# Usage:
#   install-dependencies.sh
#
# Assumptions:
#  1. You've got one or more Android SDKs and Google APIs installed locally.
#  2. Your ANDROID_HOME environment variable points to the Android SDK install directory.
#  3. You have installed the Android Repository and Google Repository libraries from the SDK installer.

set -e

ANDROID_REPOSITORY=${ANDROID_HOME}/extras/android/m2repository
GOOGLE_REPOSITORY=${ANDROID_HOME}/extras/google/m2repository
ADDONS=${ANDROID_HOME}/add-ons

function install_jar() {
  groupId=$1; artifactId=$2; version=$3; archive=$4

  if [ ! -f "$archive" ]; then
      echo "${groupId}:${artifactId} not found! Make sure that the 'Android Support Library' and 'Google Repository' is up to date in the SDK Manager."
      exit 1;
  fi

  echo "Installing ${groupId}:${artifactId} from ${archive}"
  mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=jar -Dfile="${archive}"
}

function install_aar() {
  groupId=$1; artifactId=$2; version=$3; archive=$4

  if [ ! -f "$archive" ]; then
    echo "${groupId}:${artifactId} not found! Make sure that the 'Android Support Repository' and 'Google Repository' is up to date in the SDK manager."
    exit 1
  fi

  tempdir=`mktemp -qd /tmp/robolectric-dependencies.XXXXXX`
  ( cd ${tempdir}; jar xvf ${archive} > /dev/null 2>&1 )

  echo "Installing ${groupId}:${artifactId} from ${archive}"
  mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=jar -Dfile="${tempdir}/classes.jar"
  rm -rf ${tempdir}
}

function install_maps() {
  groupId=$1; artifactId=$2 api=$3; revision=$4

  dir="${ADDONS}/addon-google_apis-google-${api}"

  if [ ! -d "$dir" ]; then
    echo "${groupId}:${artifactId} not found! Make sure that 'Google APIs' is up to date in the SDK manager for API ${api}."
    exit 1
  fi

  version=`grep ^revision= ${dir}/manifest.ini | cut -d= -f2`

  if [ "$version" != "$revision" ]; then
    echo "${groupId}:${artifactId} is an incompatible revision! Make sure that 'Google APIs' is up to date in the SDK manager for API ${api}. Expected revision ${revision} but was ${version}."
    exit 1
  fi

  echo "Installing ${groupId}:${artifactId} from ${dir}/libs/maps.jar"
  mvn -q install:install-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${api}_r${version} -Dpackaging=jar -Dfile="${dir}/libs/maps.jar"
}

if [ -z "${ANDROID_HOME}" ]; then
  echo "You must set \$ANDROID_HOME"
  exit 1
fi

# Install support-v4
install_jar "com.android.support" "support-v4" "19.0.1" "${ANDROID_REPOSITORY}/com/android/support/support-v4/19.0.1/support-v4-19.0.1.jar"

# Install multidex
install_aar "com.android.support" "multidex" "1.0.0" "${ANDROID_REPOSITORY}/com/android/support/multidex/1.0.0/multidex-1.0.0.aar"

# Install Play Services
install_aar "com.google.android.gms" "play-services" "6.5.87" "${GOOGLE_REPOSITORY}/com/google/android/gms/play-services/6.5.87/play-services-6.5.87.aar"

# Install maps
install_maps "com.google.android.maps" "maps" "18" "3"
