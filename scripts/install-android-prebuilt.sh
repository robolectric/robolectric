#!/bin/bash
#
# This script signs already built AOSP Android jars, and installs them in your local
# Maven repository. See: http://source.android.com/source/building.html for
# more information on building AOSP.
#
# Usage:
#   build-android-prebuilt.sh <jar directory path> <android version> <build_id>
#

set -eux

function usage() {
    echo "Usage: ${0} <jar dir path> <android-version> <build_id>"
}

if [[ $# -ne 3 ]]; then
    usage
    exit 1
fi

JAR_DIR=$(readlink -e "$1")
ANDROID_VERSION="$2"
BUILD_ID="$3"

SCRIPT_DIR="$(cd $(dirname "$0"); pwd)"

AA_VERSION="${ANDROID_VERSION}-robolectric-${BUILD_ID}"

TEMP_DIR="$(mktemp -d -t android-all.XXXXXXXXXX)"

STAGING_DIR="${TEMP_DIR}/staging/org/robolectric/android-all/${AA_VERSION}"

# Final artifact names
ANDROID_ALL="android-all-${AA_VERSION}.jar"
ANDROID_ALL_POM="android-all-${AA_VERSION}.pom"
ANDROID_ALL_SRC="android-all-${AA_VERSION}-sources.jar"
ANDROID_ALL_DOC="android-all-${AA_VERSION}-javadoc.jar"
ANDROID_BUNDLE="android-all-${AA_VERSION}-bundle.jar"

generate_empty_sources() {
    local TMP="$(mktemp --directory)"
    (cd "${TMP}" && jar cf "${STAGING_DIR}/${ANDROID_ALL_SRC}" .)
    rm -rf "${TMP}"
}

generate_empty_javadoc() {
    local TMP="$(mktemp --directory)"
    (cd "${TMP}" && jar cf "${STAGING_DIR}/${ANDROID_ALL_DOC}" .)
    rm -rf "${TMP}"
}

build_signed_packages() {
    echo "Robolectric: Building android-all.pom..."
    sed "s/VERSION/${AA_VERSION}/" "${SCRIPT_DIR}/pom_template.xml" | sed s/ARTIFACT_ID/android-all/ > "${STAGING_DIR}/${ANDROID_ALL_POM}"

    echo "Robolectric: Signing files with gpg..."
    for ext in ".jar" "-javadoc.jar" "-sources.jar" ".pom"; do
        (cd "${STAGING_DIR}" && gpg -ab "android-all-${AA_VERSION}$ext")
    done

    pushd "${STAGING_DIR}"
      # Generate md5, sha1, sha256, and sha512 checksums for all primary artifacts
      for f in *.pom *.jar *.asc; do
        echo "  - Generating checksums for ${f}"
        md5sum "$f" | awk '{print $1}' > "$f.md5"
        sha1sum "$f" | awk '{print $1}' > "$f.sha1"
        sha256sum "$f" | awk '{print $1}' > "$f.sha256"
        sha512sum "$f" | awk '{print $1}' > "$f.sha512"
      done
    popd

    echo "Robolectric: Creating bundle for Sonatype upload..."
    (cd "${TEMP_DIR}/staging" && zip -r "../android-all-${AA_VERSION}-bundle.zip" .)
}

mavenize() {
    local FILE_NAME_BASE=android-all-${AA_VERSION}
    mvn install:install-file \
      -Dfile="${STAGING_DIR}/${FILE_NAME_BASE}.jar" \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion="${AA_VERSION}" \
      -Dpackaging=jar \
      -DpomFile="${STAGING_DIR}/${ANDROID_ALL_POM}"

    mvn install:install-file \
      -Dfile="${STAGING_DIR}/${FILE_NAME_BASE}-sources.jar" \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion="${AA_VERSION}" \
      -Dpackaging=jar \
      -Dclassifier=sources

    mvn install:install-file \
      -Dfile="${STAGING_DIR}/${FILE_NAME_BASE}-javadoc.jar" \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion="${AA_VERSION}" \
      -Dpackaging=jar \
      -Dclassifier=javadoc
}

echo "Creating android-all package in ${TEMP_DIR}"
mkdir -p "${STAGING_DIR}"
cp "${JAR_DIR}/${ANDROID_ALL}" "${STAGING_DIR}"

generate_empty_javadoc
generate_empty_sources
build_signed_packages
mavenize

echo "Done generating bundle ${TEMP_DIR}/android-all-${AA_VERSION}-bundle.zip"
