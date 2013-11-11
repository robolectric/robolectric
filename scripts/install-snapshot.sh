#!/bin/sh
#
# Deploy a snapshot build to Sonatype.  Only non-pull requests will be deployed.
#

if [[ "${TRAVIS_PULL_REQUEST}" != "false" ]]; then
    mvn --settings scripts/mvn_settings.xml -DskipTests=true source:jar javadoc:jar deploy
fi
