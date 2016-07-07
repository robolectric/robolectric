#!/bin/bash
#
# Deploy a snapshot build to Sonatype.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}' with JDK '${TRAVIS_JDK_VERSION}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ] && [ "${TRAVIS_JDK_VERSION}" = "oraclejdk8" ]; then
    ./gradlew publish
fi
