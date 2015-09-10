#!/bin/bash
#
# This script builds the AOSP Android jars, and installs them in your local
# Maven repository. See: http://source.android.com/source/building.html for
# more information on building AOSP.
#
# Usage:
#   build-android-artifacts.sh <android version> <robolectric version>
#
# Signing Artifacts:
# The end of the script will prompt you to sign the new artifacts.  You will
# be prompted a total of 4 times (once for each artifact).  To make this
# easier, run this command beforehand:
#
#   gpg-agent --daemon
#
# It will spit out a command that you can then run in your shell to remember
# the password for the current session.
#
# Supported Versions:
#   4.1.2_r1    - Jelly Bean
#   4.2.2_r1.2  - Jelly Bean MR1
#   4.3_r2      - Jelly Bean MR2
#   4.4_r1      - Kit Kat
#   5.0.0_r2    - Lollipop
#   5.1.1_r9    - Lollipop MR1
#
# Environment Variables:
#   BUILD_ROOT        - Path to AOSP source directory
#   SIGNING_PASSWORD  - Passphrase for GPG signing key
#
# Assumptions:
#   1. You've got the full AOSP checked out on a case-sensitive file system at /Volumes/android/android-build
#   2. repo init -u https://android.googlesource.com/platform/manifest -b <android-version>
#   3. repo sync
#   4. source build/envsetup.sh
#   5. lunch aosp_x86-eng (or something like android_x86-eng)
#   6. make -j8  # probably can just run 'make -j8 snod', but we haven't tested it http://elinux.org/Android_Build_System#Make_targets
#   7. run this script
#   8. Profit!
#
if [[ $# -eq 0 ]]; then
    echo "Usage: ${0} <android-version> <robolectric-sub-version>"
    exit 1
fi

if [[ -z "${BUILD_ROOT}" ]]; then
    echo "Please set the AOSP build root path as BUILD_ROOT"
    exit 1
fi

if [[ -z "${SIGNING_PASSWORD}" ]]; then
    echo "Please set the GPG passphrase as SIGNING_PASSWORD"
    exit 1
fi

ANDROID_VERSION=$1
if [[ -z "$2" ]]; then
    ROBOLECTRIC_SUB_VERSION=0
else
    ROBOLECTRIC_SUB_VERSION=$2
fi

SCRIPT_DIR=$(cd $(dirname "$0"); pwd)

ANDROID_SOURCES_BASE=${BUILD_ROOT}
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
    elif [[ "${ANDROID_VERSION}" == "4.4_r1" ]]; then
        ARTIFACTS=("core" "services" "telephony-common" "framework" "framework2" "framework-base" "android.policy" "ext" "webviewchromium")
    elif [[ "${ANDROID_VERSION}" == "5.0.0_r2" ]]; then
        ARTIFACTS=("core-libart" "services" "telephony-common" "framework" "android.policy" "ext")
    elif [[ "${ANDROID_VERSION}" == "5.1.1_r9" ]]; then
        ARTIFACTS=("core-libart" "services" "telephony-common" "framework" "android.policy" "ext")
    else
        echo "Robolectric: No match for version: ${ANDROID_VERSION}"
        exit 1
    fi

    cd ${ANDROID_SOURCES_BASE}
    if [ ! -d out/target/common/obj/JAVA_LIBRARIES ]; then
      echo "Robolectric: You need to run 'make -j8' first"
      exit 1
    fi
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
    build_tzdata
    build_jarjared_classes
    cd ${OUT}/android-all-classes; jar cf ${OUT}/${ANDROID_CLASSES} .
    rm -rf ${OUT}/android-all-classes
}

build_tzdata() {
    echo "Robolectric: Building tzdata..."
    mkdir -p ${OUT}/android-all-classes/usr/share/zoneinfo
    cp ${ANDROID_SOURCES_BASE}/out/target/product/generic_x86_64/system/usr/share/zoneinfo/tzdata ${OUT}/android-all-classes/usr/share/zoneinfo
}

build_jarjared_classes() {
    echo "Robolectric: jarjaring classes..."
    mkdir ${OUT}/android-jarjar-workspace
    cd ${OUT}/android-jarjar-workspace
    wget https://jarjar.googlecode.com/files/jarjar-1.4.jar
    echo "rule com.squareup.** com.android.**" >> rules
    echo "rule org.conscrypt.** com.android.**" >> rules

    java -jar jarjar-1.4.jar process rules ${ANDROID_SOURCES_BASE}/out/target/common/obj/JAVA_LIBRARIES/okhttp_intermediates/classes.jar okhttp-classes.jar
    java -jar jarjar-1.4.jar process rules ${ANDROID_SOURCES_BASE}/out/target/common/obj/JAVA_LIBRARIES/conscrypt_intermediates/classes.jar conscrypt-classes.jar

    cd ${OUT}/android-all-classes;
    jar xf ${OUT}/android-jarjar-workspace/okhttp-classes.jar
    jar xf ${OUT}/android-jarjar-workspace/conscrypt-classes.jar
}

build_android_all_jar() {
    echo "Robolectric: Building android-all..."
    mkdir ${OUT}/android-all
    cd ${OUT}/android-all; jar xf ${OUT}/${ANDROID_RES}
    cd ${OUT}/android-all; jar xf ${OUT}/${ANDROID_EXT}
    cd ${OUT}/android-all; jar xf ${OUT}/${ANDROID_CLASSES}

    # Remove unused files
    rm -rf ${OUT}/android-all/Android.mk
    rm -rf ${OUT}/android-all/AndroidManifest.xml
    rm -rf ${OUT}/android-all/META-INF
    rm -rf ${OUT}/android-all/MODULE_LICENSE_APACHE2
    rm -rf ${OUT}/android-all/MakeJavaSymbols.sed
    rm -rf ${OUT}/android-all/NOTICE
    rm -rf ${OUT}/android-all/lint.xml
    rm -rf ${OUT}/android-all/java/lang

    # Build the new JAR file
    cd ${OUT}/android-all; jar cf ${OUT}/${ANDROID_ALL} .
    rm ${OUT}/${ANDROID_RES} ${OUT}/${ANDROID_EXT} ${OUT}/${ANDROID_CLASSES}
}

build_android_src_jar() {
    echo "Robolectric: Building android-all-source..."
    local src=${ANDROID_SOURCES_BASE}/frameworks/base
    local tmp=${OUT}/sources
    mkdir ${tmp}
    rsync -va ${src}/core/java/ ${tmp}/
    rsync -va ${src}/graphics/java/ ${tmp}/
    rsync -va ${src}/media/java/ ${tmp}/
    rsync -va ${src}/location/java/ ${tmp}/
    rsync -va ${src}/opengl/java/ ${tmp}/
    rsync -va ${src}/policy/src/ ${tmp}/
    rsync -va ${src}/sax/java/ ${tmp}/
    rsync -va ${src}/services/java/ ${tmp}/
    rsync -va ${src}/telephony/java/ ${tmp}/
    rsync -va ${src}/wifi/java/ ${tmp}/
    rsync -va ${ANDROID_SOURCES_BASE}/libcore/luni/src/main/java/ ${tmp}/ # this is new
    cd ${tmp}; jar cf ${OUT}/${ANDROID_ALL_SRC} .
    rm -rf ${tmp}
}

build_android_doc_jar() {
    # TODO: Actually build the docs
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
        ( cd ${OUT} && gpg -ab --passphrase ${SIGNING_PASSWORD} android-all-${ROBOLECTRIC_VERSION}$ext )
    done

    echo "Robolectric: Creating bundle for Sonatype upload..."
    cd ${OUT}; jar cf ${ANDROID_BUNDLE} *.jar *.pom *.asc
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

OUT=`mktemp --directory`
mkdir -p ${OUT}

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

