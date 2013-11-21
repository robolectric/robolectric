#!/bin/sh
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

BRANCH=`git rev-parse --abbrev-ref HEAD`
echo "Pull request: '${TRAVIS_PULL_REQUEST}' on branch '${BRANCH}'"
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${BRANCH}" = "master" ]; then
    mvn --settings scripts/mvn_settings.xml -DskipTests=true source:jar javadoc:jar deploy
fi
