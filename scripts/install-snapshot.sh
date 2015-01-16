#!/bin/bash
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

PROJECT=$(cd $(dirname "$0")/..; pwd)
ARGUMENTS="--settings $PROJECT/scripts/mvn_settings.xml -DskipTests"

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then

    echo "Cleaning dist directories..."
    cd "$PROJECT"; mvn ${ARGUMENTS} clean -Pdist

    # Install older shadow packages
    echo "Building shadows for API 15..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity source:jar javadoc:jar package -Pandroid-15

    echo "Building shadows for API 16..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity source:jar javadoc:jar package -Pandroid-16

    echo "Building shadows for API 17..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity source:jar javadoc:jar package -Pandroid-17

    echo "Building shadows for API 18..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity source:jar javadoc:jar package -Pandroid-18

    echo "Building shadows for API 19..."
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity source:jar javadoc:jar package -Pandroid-19

    # Install everything
    echo "Cleaning project..."
    cd "$PROJECT"; mvn ${ARGUMENTS} clean

    echo "Building API 21 and uploading artifacts to Sonatype..."
    cd "$PROJECT"; mvn ${ARGUMENTS} source:jar javadoc:jar deploy -Pupload,android-21
fi
