#!/bin/bash
#
# Deploy a snapshot build to Sonatype.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)
ARGUMENTS="--settings $PROJECT/scripts/mvn_settings.xml -D skipTests"

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}' with JDK '${TRAVIS_JDK_VERSION}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ] && [ "${TRAVIS_JDK_VERSION}" = "oraclejdk8" ]; then

    echo "Building Robolectric..."
    cd "$PROJECT"; mvn ${ARGUMENTS} -P snapshot clean deploy

    echo "Building shadows for API 16..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} -P snapshot,android-16 clean package

    echo "Building shadows for API 17..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} -P snapshot,android-17 clean package

    echo "Building shadows for API 18..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} -P snapshot,android-18 clean package

    echo "Building shadows for API 19..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} -P snapshot,android-19 clean package

    echo "Building shadows for API 21..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} -P snapshot,android-21 clean package

    echo "Uploading SNAPSHOT..."
    cd "$PROJECT"; mvn ${ARGUMENTS} -P snapshot,android-latest,upload deploy
fi
