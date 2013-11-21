#!/bin/sh
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${TRAVIS_BRANCH}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then
    mvn --settings scripts/mvn_settings.xml -DskipTests=true source:jar javadoc:jar deploy
fi
