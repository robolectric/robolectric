#!/bin/bash

set -e

function repackage() {
  inJar=$( realpath "$1" )

  tmpDir=$( mktemp -d -t repackage-android.XXXXXXXXXX )

  metaFile="META-INF/android-sdk.properties"
  contentsDir="_SDK_"

  pwd=$( pwd )

  (
    cd "$tmpDir"
    jar xf "$inJar" build.prop

    function getProperty() {
      propName=$1

      grep "^${propName}=" build.prop | cut -d '=' -f 2-
    }


    files=(*)

    mkdir -p "$( dirname "$metaFile" )"
    echo "path=${contentsDir}" >> "${metaFile}"

    sdkInt="$( getProperty ro.build.version.sdk )"      # 16
    release="$( getProperty ro.build.version.release )" # 4.1.2
    buildId="$( getProperty ro.build.version.incremental )"

    echo "sdk=$sdkInt" >> "${metaFile}"

    # ro.build.version.incremental=eng.brettchabot.20171005.132931


    echo "codename=$( getProperty ro.build.version.codename )" >> "${metaFile}" # REL

    echo "release=$release" >> "${metaFile}" # 4.1.2

    cat "${metaFile}"


    artifactId="android-sdk-$sdkInt"
    version="$( getProperty ro.build.version.release )"

    echo "org.robolectric:${artifactId}:${version}"



    mkdir "${contentsDir}"
    for file in "${files[@]}"; do
      if [[ "$file" != "META-INF" ]]; then
        mv "$file" "${contentsDir}"
      fi
    done

    mv "${contentsDir}/res" .
    mv "${contentsDir}/resources.arsc" .
    mv "${contentsDir}/META-INF" .
    jar cf "$pwd/lib/repackaged-$( basename $inJar )" *
  )

  rm -rf "$tmpDir"
}

files=$*

if [ -z "$files" ]; then
  echo "Usage: $0 android-all.jars..."
  exit
else
  for file in "$files"; do
    repackage "$file"
  done
fi

