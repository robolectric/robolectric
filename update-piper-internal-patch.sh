#!/bin/bash

# Updates piper-internal.patch with current internal changes.
#
# Note: Its highly recommended to first run the copybara bulk-piper-to-github
# workflow, to ensure piper and github are in sync except for internal changes.
#
# Usage:
#   ./update-piper-internal-patch.sh [CL]
#   Must be run from current directory
#   (google3/third_party/java_src/robolectric)

set -ex

g3_dir=$(pwd)
local_git_folder=$(mktemp -d)
if [[ $# -eq 1 ]]; then
  CL=$1
  /google/data/ro/teams/copybara/copybara copy.bara.sky generate-piper-diff-from-cl $CL --force --git-destination-skip-push --git-destination-path $local_git_folder
else
  /google/data/ro/teams/copybara/copybara copy.bara.sky generate-piper-diff --force --git-destination-skip-push --git-destination-path $local_git_folder
fi

cd $local_git_folder
git diff HEAD~1 > $g3_dir/piper-internal.patch

echo "Updated $g3_dir/piper-internal.patch!"

