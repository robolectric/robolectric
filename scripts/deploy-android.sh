#!/bin/bash
#
# This script deploys/publishes a built AOSP Android jars to remote maven
#
# Usage:
#   deploy-android.sh <jar path> <android version> <robolectric version>
#
# For a tutorial check scripts/README.md

set -ex

function usage() {
    echo "Usage: ${0} <artifact path> <android-version> <robolectric-sub-version>"
}

if [[ $# -ne 3 ]]; then
    usage
    exit 1
fi

ARTIFACT_PATH=$1
ANDROID_VERSION=$2
ROBOLECTRIC_SUB_VERSION=$3

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

ROBOLECTRIC_VERSION=${ANDROID_VERSION}-robolectric-${ROBOLECTRIC_SUB_VERSION}

# Final artifact names
ANDROID_ALL=android-all-${ROBOLECTRIC_VERSION}.jar
ANDROID_ALL_POM=android-all-${ROBOLECTRIC_VERSION}.pom
ANDROID_ALL_SRC=android-all-${ROBOLECTRIC_VERSION}-sources.jar
ANDROID_ALL_DOC=android-all-${ROBOLECTRIC_VERSION}-javadoc.jar
ANDROID_BUNDLE=android-all-${ROBOLECTRIC_VERSION}-bundle.jar


mavenize() {
    local FILE_NAME_BASE=android-all-${ROBOLECTRIC_VERSION}
    mvn deploy:deploy-file \
      -Dfile=${ARTIFACT_PATH}/${FILE_NAME_BASE}.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar

    mvn deploy:deploy-file \
      -Dfile=${ARTIFACT_PATH}/${FILE_NAME_BASE}-sources.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=sources

    mvn deploy:deploy-file \
      -Dfile=${ARTIFACT_PATH}/${FILE_NAME_BASE}-javadoc.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=javadoc
}

mavenize

echo "DONE!!"
