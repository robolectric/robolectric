#!/bin/bash
#
# Install Robolectric into the local Maven repository.
#
set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

build_robolectric() {
  echo "Building Robolectric..."
  cd "$PROJECT"
  ./gradlew clean assemble publishToMavenLocal compileTest --info --stacktrace
}

run_tests() {
  echo "Running Tests..."
  cd "$PROJECT"
  ./gradlew --continue test --info --stacktrace
}

build_robolectric
run_tests
echo "Installation successful!"