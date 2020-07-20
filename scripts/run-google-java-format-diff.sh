#!/bin/bash

set -e

release=google-java-format-1.8
tmpdir="/tmp/$release"

mkdir -p $tmpdir

# Download the script in a subshell for a safe directory change
(
  cd $tmpdir
  if ! [ -f google-java-format.jar ]; then
    curl -L -o google-java-format.jar "https://github.com/google/google-java-format/releases/download/$release/$release-all-deps.jar"
  fi

  if ! [ -f google-java-format-diff.py ]; then
    curl -L -O "https://raw.githubusercontent.com/google/google-java-format/$release/scripts/google-java-format-diff.py"
  fi
)

# Run the script at the git root
(
  cd $(git rev-parse --show-toplevel)
  echo "Running google-java-format-diff.py"
  git diff -U0 HEAD^ | python $tmpdir/google-java-format-diff.py --google-java-format-jar=$tmpdir/google-java-format.jar -p1 -i
)

