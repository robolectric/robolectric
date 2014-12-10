#!/bin/bash
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

PROJECT=$(cd $(dirname "$0")/..; pwd)
ARGUMENTS="--settings $PROJECT/scripts/mvn_settings.xml -DskipTests"

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then

    # Install everything
    cd "$PROJECT"; mvn ${ARGUMENTS} deploy

    # Install older shadow packages
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity deploy -Pandroid-15
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity deploy -Pandroid-16
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity deploy -Pandroid-17
    cd "$PROJECT"/robolectric-shadows/shadows-core; mvn ${ARGUMENTS} clean velocity:velocity deploy -Pandroid-18
fi
