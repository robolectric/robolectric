#!/bin/bash
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

PROJECT=$(cd $(dirname "$0")/..; pwd)
ARGUMENTS="--settings $PROJECT/scripts/mvn_settings.xml -DskipTests"

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}' with JDK '${TRAVIS_JDK_VERSION}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ] && [ "${TRAVIS_JDK_VERSION}" = "oraclejdk8" ]; then

    echo "Cleaning dist directories..."
    cd "$PROJECT"; mvn ${ARGUMENTS} clean -Pdist

    echo "Building shadows for API 16..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean package source:jar javadoc:jar -Pandroid-16

    echo "Building shadows for API 17..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean package source:jar javadoc:jar -Pandroid-17

    echo "Building shadows for API 18..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean package source:jar javadoc:jar -Pandroid-18

    echo "Building shadows for API 19..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean package source:jar javadoc:jar -Pandroid-19

    echo "Building Robolectric and uploading artifacts to Sonatype..."
    cd "$PROJECT"; mvn ${ARGUMENTS} clean package source:jar javadoc:jar deploy -Pupload,android-21
fi
