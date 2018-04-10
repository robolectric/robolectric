#!/bin/bash

set -e

#currentVersion=android-8.0.0_r36
currentVersion=android-8.1.0_r22

baseDir=`dirname $0`/..
frameworksBaseRepoDir="$HOME/Dev/Android/base"

function showDiffs2() {
  file="$1"
  line="$2"

  x=$(echo "$line" | sed -e 's/.*https:\/\/android.googlesource.com\/\([^ ]*\)\/[+]\/\([^/]*\)\/\([^ ]*\).*/\1 \2 \3/')
  IFS=" " read -a parts <<< "$x"
  repo="${parts[0]}"
  version="${parts[1]}"
  file="${parts[2]}"

  curSha=$(cd "$frameworksBaseRepoDir" && git rev-parse --verify "$currentVersion") || true
  if [[ -z "$curSha" ]]; then
    echo "Unknown $currentVersion!"
    exit 1
  fi

  thisSha=$(cd "$frameworksBaseRepoDir" && git rev-parse --verify "$version") || true
  if [[ -z "$thisSha" ]]; then
    echo "Unknown $version!"
    return
  fi

  if [ "x$curSha" != "x$thisSha" ]; then
    echo "Apply changes to: $file"
    (cd "$frameworksBaseRepoDir" && git diff "${version}..${currentVersion}" "$file")
  fi
}

function showDiffs() {
  file="$1"

  grep "https://android.googlesource.com" "$file" | while read -r line ; do
    showDiffs2 "$file" "$line"
  done
}

files=$*

if [ -z "$files" ]; then
  find . -name "*.java" -print0 | while read -d $'\0' file; do
    showDiffs "$file"
  done
else
  for file in "$files"; do
    showDiffs "$file"
  done
fi