#!/bin/bash

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

cd "$PROJECT"; mvn clean
cd "$PROJECT"/robolectric-utils; mvn clean install
cd "$PROJECT"/robolectric-annotations; mvn clean install
cd "$PROJECT"/robolectric-processor; mvn clean install
cd "$PROJECT"/robolectric-resources; mvn clean install

# Build core shadows
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-15
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-16
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-17
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-18
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-19

# Build robolectric (tests depend on shadows-core)
cd "$PROJECT"/robolectric; mvn install -DskipTests

# Build add-on shadows
cd "$PROJECT"/robolectric-shadows/shadows-httpclient; mvn install
cd "$PROJECT"/robolectric-shadows/shadows-support-v4; mvn install

# Run the robolectric tests (and build docs / sources)
cd "$PROJECT"; mvn javadoc:javadoc source:jar install
