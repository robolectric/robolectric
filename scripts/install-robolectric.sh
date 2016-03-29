#!/bin/bash
#
# Install Robolectric into the local Maven repository.
#
set -e

APIS=(16 17 18 19 20 21 22 23)
PARALLEL_BUILD_CONFIG="0.5C"

PROJECT=$(cd $(dirname "$0")/..; pwd)
if [ -z ${INCLUDE_SOURCE+x} ]; then SOURCE_ARG=""; else SOURCE_ARG="source:jar"; fi
if [ -z ${INCLUDE_JAVADOC+x} ]; then JAVADOC_ARG=""; else JAVADOC_ARG="javadoc:jar"; fi

build_robolectric() {
  echo "Building Robolectric..."
  cd "$PROJECT"
  mvn -T ${PARALLEL_BUILD_CONFIG} -D skipTests clean ${SOURCE_ARG} ${JAVADOC_ARG} install
}

build_shadows() {
  for api in "${APIS[@]}"
  do
     build_shadows_for_api ${api}
  done
}

build_shadows_for_api() {
  echo "Building shadows for API ${1}..."
  cd "$PROJECT"/robolectric-shadows/shadows-core
  mvn -T ${PARALLEL_BUILD_CONFIG} -P android-${1} clean ${SOURCE_ARG} ${JAVADOC_ARG} install
}

run_tests() {
  echo "Running Tests..."
  cd "$PROJECT"
  mvn -T ${PARALLEL_BUILD_CONFIG} test
}

build_robolectric
build_shadows
run_tests
echo "Installation successful!"