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
    jar xf "$inJar"

    files=(*)
    mkdir "${contentsDir}"
    for file in "${files[@]}"; do
      mv "$file" "${contentsDir}"
    done

    declare -A buildProps

    while IFS='' read -r line || [[ -n "$line" ]]; do
      if [[ $line =~ ^[a-zA-Z] ]]; then
        i=$( expr index "$line" = )
        key=${line:0:$i-1}
        value=${line:$i}
        buildProps["$key"]="$value"
      fi
    done < "${contentsDir}/build.prop"

    echo "path=${contentsDir}" >> "${metaFile}"
    echo "sdk=${buildProps[ro.build.version.sdk]}" >> "${metaFile}"
    echo "buildId=${buildProps[ro.build.id]}" > "${metaFile}"
    echo "codename=${buildProps[ro.build.version.codename]}" >> "${metaFile}"

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

