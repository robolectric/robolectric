#!/bin/bash

set -e

#currentVersion=android-8.0.0_r36
#currentVersion=android-8.1.0_r22
currentVersion=android-9.0.0_r12

baseDir=`dirname $0`/..
frameworksBaseRepoDir="$HOME/Dev/AOSP/frameworks/base"

function showDiffs2() {
  file="$1"
  line="$2"

  x=$(echo "$line" | sed -e 's/.*https:\/\/android.googlesource.com\/\([^ ]*\)\/[+]\/\([^/]*\)\/\([^ ]*\).*/\1 \2 \3/')
  IFS=" " read -a parts <<< "$x"
  repo="${parts[0]}"
  version="${parts[1]}"
  repoFile="${parts[2]}"

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
    (cd "$frameworksBaseRepoDir" && git diff --quiet "${version}..${currentVersion}" "--" "$repoFile")
    if [ $? -eq 0 ]; then
      echo "No changes in: $file"
      echo "  From $repoFile $version -> $currentVersion"
    else
      tmpFile="/tmp/diff.tmp"
      rm -f "$tmpFile"
      echo "Apply changes to: $file" > "$tmpFile"
      echo "  From $repoFile $version -> $currentVersion" >> "$tmpFile"
      (cd "$frameworksBaseRepoDir" && git diff --color=always "${version}..${currentVersion}" "--" "$repoFile" >> "$tmpFile")
      less -r "$tmpFile"
    fi
  fi
}

function showDiffs() {
  file="$1"

  grep -E 'https?:\/\/(android\.googlesource\.com|.*\.git\.corp\.google\.com)\/' "$file" | \
      while read -r line ; do
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