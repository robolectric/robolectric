@@ -0,0 1,85 @@
#!/bin/bash
#
# This script signs already built AOSP Android jars, and installs them in your local
# Maven repository. See: http://source.android.com/source/building.html for
# more information on building AOSP.
#
# Usage:
#   build-android-prebuilt.sh <jar directory path> <android version> <robolectric version>
#

set -ex

function usage() {
    echo "Usage: ${0} <jar dir path> <android-version> <robolectric-sub-version>"
}

if [[ $# -ne 3 ]]; then
    usage
    exit 1
fi

if [[ -z "${SIGNING_PASSWORD}" ]]; then
    echo "Please set the GPG passphrase as SIGNING_PASSWORD"
    exit 1
fi

JAR_DIR=$1
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

generate_empty_src_javadoc() {
    TMP=`mktemp --directory`
    cd ${TMP}
    jar cf ${JAR_DIR}/${ANDROID_ALL_DOC} .
    jar cf ${JAR_DIR}/${ANDROID_ALL_SRC} .
    cd ${JAR_DIR}; rm -rf ${TMP}
}

build_signed_packages() {
    echo "Robolectric: Building android-all.pom..."
    sed s/VERSION/${ROBOLECTRIC_VERSION}/ ${SCRIPT_DIR}/pom_template.xml | sed s/ARTIFACT_ID/android-all/ > ${JAR_DIR}/${ANDROID_ALL_POM}

    echo "Robolectric: Signing files with gpg..."
    for ext in ".jar" "-javadoc.jar" "-sources.jar" ".pom"; do
        ( cd ${JAR_DIR} && gpg -ab --passphrase ${SIGNING_PASSWORD} android-all-${ROBOLECTRIC_VERSION}$ext )
    done

    echo "Robolectric: Creating bundle for Sonatype upload..."
    cd ${JAR_DIR}; jar cf ${ANDROID_BUNDLE} *.jar *.pom *.asc
}

mavenize() {
    local FILE_NAME_BASE=android-all-${ROBOLECTRIC_VERSION}
    mvn install:install-file \
      -Dfile=${JAR_DIR}/${FILE_NAME_BASE}.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar

    mvn install:install-file \
      -Dfile=${JAR_DIR}/${FILE_NAME_BASE}-sources.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=sources

    mvn install:install-file \
      -Dfile=${JAR_DIR}/${FILE_NAME_BASE}-javadoc.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=javadoc
}

generate_empty_src_javadoc
build_signed_packages
mavenize

echo "DONE!!"