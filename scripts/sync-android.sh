#!/bin/bash
#
# This script sync and builds the AOSP source for a specified platform version.
#
# Usage:
#   sync-android.sh <src root> <android version>
#
# This will create a <src root>/aosp-<android version>, sync the source for that version, and
# attempt to build.
#
# You may need to customize the JAVA_6 or JAVA_7 install locations environment variables, or ensure
# the right version of java is in your PATH when versions earlier than nougat. See
# https://source.android.com/source/requirements#jdk for more details.
#
# See README.md for additional instructions

JAVA_6=/usr/lib/jvm/jdk1.6.0_45/bin
JAVA_7=/usr/lib/jvm/java-7-openjdk-amd64/bin

function usage() {
    echo "Usage: ${0} <android root path> <android-version> <parallel jobs>"
}

if [[ $# -ne 3 ]]; then
    usage
    exit 1
fi

set -ex

ANDROID_VERSION=$2
SRC_ROOT=$1/aosp-$ANDROID_VERSION
J=$3

sync_source() {
    repo init -q --depth=1 -uhttps://android.googlesource.com/platform/manifest -b android-$ANDROID_VERSION
    repo sync -cq -j$J
}

build_source() {
    source build/envsetup.sh

    if [[ "${ANDROID_VERSION}" == "4.1.2_r1" ]]; then
        lunch generic_x86-eng
        export PATH=$JAVA_6:$PATH
        make -j$J
    elif [[ "${ANDROID_VERSION}" == "4.2.2_r1.2" ]]; then
        lunch generic_x86-eng
        export PATH=$JAVA_6:$PATH
        make -j$J
    elif [[ "${ANDROID_VERSION}" == "4.3_r2" ]]; then
        lunch aosp_x86-eng
        export PATH=$JAVA_6:$PATH
        make -j$J
    elif [[ "${ANDROID_VERSION}" == "4.4_r1" ]]; then
        lunch aosp_x86-eng
        export PATH=$JAVA_6:$PATH
        make -j$J
    elif [[ "${ANDROID_VERSION}" == "5.0.2_r3" ]]; then
        tapas core-libart services services.accessibility telephony-common framework ext framework-res
        export PATH=$JAVA_7:$PATH
        ANDROID_COMPILE_WITH_JACK=false make -j$J
    elif [[ "${ANDROID_VERSION}" == "5.1.1_r9" ]]; then
        tapas core-libart services services.accessibility telephony-common framework ext framework-res
        export PATH=$JAVA_7:$PATH
        ANDROID_COMPILE_WITH_JACK=false make -j$J
    elif [[ "${ANDROID_VERSION}" == "6.0.1_r3" ]]; then
        tapas core-libart services services.accessibility telephony-common framework ext icu4j-icudata-jarjar framework-res
        export PATH=$JAVA_7:$PATH
        ANDROID_COMPILE_WITH_JACK=false make -j$J
    elif [[ "${ANDROID_VERSION}" == "7.0.0_r1" ]]; then
        cd ../..
        lunch aosp_x86-eng
        make -j$J
        make -j$J out/target/common/obj/JAVA_LIBRARIES/services_intermediates/classes.jar out/host/linux-x86/framework/icu4j-icudata-host-jarjar.jar out/host/linux-x86/framework/icu4j-icutzdata-host-jarjar.jar
    elif [[ "${ANDROID_VERSION}" == "7.1.0_r7" ]]; then
        cd frameworks/base && git fetch https://android.googlesource.com/platform/frameworks/base refs/changes/75/310575/1 && git cherry-pick FETCH_HEAD && git commit -a -m "patch shortcut service"
        cd ../..
        lunch aosp_x86-eng
        make -j$J
        make -j$J out/target/common/obj/JAVA_LIBRARIES/services_intermediates/classes.jar out/host/linux-x86/framework/icu4j-icudata-host-jarjar.jar out/host/linux-x86/framework/icu4j-icutzdata-host-jarjar.jar
    elif [[ "${ANDROID_VERSION}" == "8.0.0_r4" ]]; then
        cd external/robolectric && git fetch https://android.googlesource.com/platform/external/robolectric refs/changes/22/463722/1 && git cherry-pick FETCH_HEAD
        cd ../..
        lunch aosp_x86-eng
        make -j$J robolectric_android-all
    else
        echo "Robolectric: No match for version: ${ANDROID_VERSION}"
        exit 1
    fi
}

mkdir -p $SRC_ROOT
cd $SRC_ROOT

sync_source
build_source

echo "Done building $SRC_ROOT!!"

