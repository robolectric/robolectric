#!/bin/bash

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

cd "$PROJECT"; mvn clean

cd "$PROJECT"/robolectric-annotations; mvn clean install
cd "$PROJECT"/robolectric-utils; mvn clean install
cd "$PROJECT"/robolectric-resources; mvn clean install
cd "$PROJECT"/robolectric-processor; mvn clean install

cd "$PROJECT"/robolectric; mvn install -DskipTests

cd "$PROJECT"/robolectric-shadows/shadows-httpclient; mvn install -DskipTests;
cd "$PROJECT"/robolectric-shadows/shadows-support-v4; mvn install -DskipTests

cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-15 -DskipTests
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-16 -DskipTests
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-17 -DskipTests
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-18 -DskipTests
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-19 -DskipTests

cd "$PROJECT"; mvn javadoc:javadoc source:jar install
