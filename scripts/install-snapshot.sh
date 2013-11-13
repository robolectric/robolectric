#!/bin/sh
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${BRANCH}" = "master" ]; then
    mvn --settings scripts/mvn_settings.xml -DskipTests=true source:jar javadoc:jar deploy
fi
