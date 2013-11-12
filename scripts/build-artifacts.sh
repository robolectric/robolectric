#!/bin/sh
#
# This script builds the AOSP Android jars, and installs them in your local
# Maven repository. See: http://source.android.com/source/building.html for
# more information on building AOSP.
#
# Usage:
#   build-android-artifacts.sh <android version> <robolectric version>
#
# Supported Versions:
#   4.1.2_r1    - Jelly Bean
#   4.2.2_r1.2  - Jelly Bean MR1
#   4.3_r2      - Jelly Bean MR2
#
# Assumptions:
#  1. You've got the full AOSP checked out on a case-sensitive file system at /Volumes/android/android-build
#  2. repo init -u https://android.googlesource.com/platform/manifest -b <android-version>
#  3. repo sync
#  4. source build/envsetup.sh
#  5. lunch aosp_x86-eng (or something like android_x86-eng)
#
if [[ $# -eq 0 ]]; then
    echo "Usage: ${0} <android-version> <robolectric-sub-version>"
    exit 1
fi

ANDROID_VERSION=$1
if [[ -z "$2" ]]; then
    ROBOLECTRIC_SUB_VERSION=0
else
    ROBOLECTRIC_SUB_VERSION=$2
fi

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

ANDROID_SOURCES_BASE=/Volumes/android/android-build
FRAMEWORKS_BASE_DIR=${ANDROID_SOURCES_BASE}/frameworks/base
ROBOLECTRIC_VERSION=${ANDROID_VERSION}-robolectric-${ROBOLECTRIC_SUB_VERSION}

# Intermediate artifacts
ANDROID_RES=android-res-${ANDROID_VERSION}.jar
ANDROID_EXT=android-ext-${ANDROID_VERSION}.jar
ANDROID_CLASSES=android-classes-${ANDROID_VERSION}.jar

# Final artifact names
ANDROID_ALL=android-all-${ROBOLECTRIC_VERSION}.jar
ANDROID_ALL_POM=android-all-${ROBOLECTRIC_VERSION}.pom
ANDROID_ALL_SRC=android-all-${ROBOLECTRIC_VERSION}-sources.jar
ANDROID_ALL_DOC=android-all-${ROBOLECTRIC_VERSION}-javadoc.jar
ANDROID_BUNDLE=android-all-${ROBOLECTRIC_VERSION}-bundle.jar

build_platform() {
    if [[ "${ANDROID_VERSION}" == "4.1.2_r1" ]]; then
        ARTIFACTS=("core" "services" "framework" "android.policy" "ext")
    elif [[ "${ANDROID_VERSION}" == "4.2.2_r1.2" ]]; then
        ARTIFACTS=("core" "services" "telephony-common" "framework" "android.policy" "ext")
    elif [[ "${ANDROID_VERSION}" == "4.3_r2" ]]; then
        ARTIFACTS=("core" "services" "telephony-common" "framework" "android.policy" "ext")
    else
        echo "Robolectric: No match for version: ${ANDROID_VERSION}"
        exit 1
    fi

    echo "Robolectric: Building artifacts: ${ARTIFACTS[@]}"
    cd ${ANDROID_SOURCES_BASE}
    make clean; make -j8 ${ARTIFACTS[@]}
}

build_android_res() {
    echo "Robolectric: Building android-res..."
    cd ${FRAMEWORKS_BASE_DIR}/core/res; jar cf ${OUT}/${ANDROID_RES} .
}

build_android_ext() {
    echo "Robolectric: Building android-ext..."
    local PHONE_NUMBERS_DIR=com/android/i18n/phonenumbers
    mkdir -p ${OUT}/ext-classes-modified/${PHONE_NUMBERS_DIR}
    cd ${OUT}/ext-classes-modified; jar xf ${ANDROID_SOURCES_BASE}/out/target/common/obj/JAVA_LIBRARIES/ext_intermediates/classes.jar
    cp -R ${ANDROID_SOURCES_BASE}/external/libphonenumber/java/src/${PHONE_NUMBERS_DIR}/data ${OUT}/ext-classes-modified/${PHONE_NUMBERS_DIR}
    cd ${OUT}/ext-classes-modified; jar cf ${OUT}/${ANDROID_EXT} .
    rm -rf ${OUT}/ext-classes-modified
}

build_android_classes() {
    echo "Robolectric: Building android-classes..."
    mkdir ${OUT}/android-all-classes
    for artifact in "${ARTIFACTS[@]}"; do
        src=${ANDROID_SOURCES_BASE}/out/target/common/obj/JAVA_LIBRARIES/${artifact}_intermediates
        cd ${OUT}/android-all-classes; jar xf ${src}/classes.jar
    done
    cd ${OUT}/android-all-classes; jar cf ${OUT}/${ANDROID_CLASSES} .
    rm -rf ${OUT}/android-all-classes
}

build_android_all_jar() {
    echo "Robolectric: Building android-all..."
    mkdir ${OUT}/android-all-classes
    cd ${OUT}/android-all-classes; jar xf ${OUT}/${ANDROID_RES}
    cd ${OUT}/android-all-classes; jar xf ${OUT}/${ANDROID_EXT}
    cd ${OUT}/android-all-classes; jar xf ${OUT}/${ANDROID_CLASSES}
    cd ${OUT}/android-all-classes; jar cf ${OUT}/${ANDROID_ALL} .
    rm -rf ${OUT}/android-all-classes
    rm ${OUT}/${ANDROID_RES} ${OUT}/${ANDROID_EXT} ${OUT}/${ANDROID_CLASSES}
}

build_android_src_jar() {
    echo "Robolectric: Building android-all-source..."
    local src=${ANDROID_SOURCES_BASE}/frameworks/base
    local tmp=${OUT}/sources
    mkdir ${tmp}
    cp -R ${src}/core/java/ ${tmp}
    cp -R ${src}/graphics/java/ ${tmp}
    cp -R ${src}/media/java/ ${tmp}
    cp -R ${src}/location/java/ ${tmp}
    cp -R ${src}/opengl/java/ ${tmp}
    cp -R ${src}/policy/src/ ${tmp}
    cp -R ${src}/sax/java/ ${tmp}
    cp -R ${src}/services/java/ ${tmp}
    cp -R ${src}/telephony/java/ ${tmp}
    cp -R ${src}/wifi/java/ ${tmp}
    cd ${tmp}; jar cf ${OUT}/${ANDROID_ALL_SRC} .
    rm -rf ${tmp}
}

build_android_doc_jar() {
    echo "Robolectric: Building android-all-javadoc..."
    mkdir ${OUT}/javadoc
    cd ${OUT}/javadoc; jar cf ${OUT}/${ANDROID_ALL_DOC} .
    rm -rf ${OUT}/javadoc
}

build_signed_packages() {
    echo "Robolectric: Building android-all.pom..."
    sed s/VERSION/${ROBOLECTRIC_VERSION}/ ${SCRIPT_DIR}/pom_template.xml | sed s/ARTIFACT_ID/android-all/ > ${OUT}/${ANDROID_ALL_POM}

    echo "Robolectric: Signing files with gpg..."
    for ext in ".jar" "-javadoc.jar" "-sources.jar" ".pom"; do
        ( cd ${OUT} && gpg -ab --use-agent android-all-${ROBOLECTRIC_VERSION}$ext )
    done

    echo "Robolectric: Creating bundle for Sonatype upload..."
    cd ${OUT}; jar cf ${ANDROID_BUNDLE} *
}

mavenize() {
    local FILE_NAME_BASE=android-all-${ROBOLECTRIC_VERSION}
    mvn install:install-file \
      -Dfile=${OUT}/${FILE_NAME_BASE}.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar

    mvn install:install-file \
      -Dfile=${OUT}/${FILE_NAME_BASE}-sources.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=sources

    mvn install:install-file \
      -Dfile=${OUT}/${FILE_NAME_BASE}-javadoc.jar \
      -DgroupId=org.robolectric \
      -DartifactId=android-all \
      -Dversion=${ROBOLECTRIC_VERSION} \
      -Dpackaging=jar \
      -Dclassifier=javadoc
}

OUT=`mktemp -t mavenize-android -d`
build_platform
build_android_res
build_android_ext
build_android_classes
build_android_all_jar
build_android_src_jar
build_android_doc_jar
build_signed_packages
mavenize

echo "DONE!!"
echo "Your artifacts are located here: ${OUT}"
