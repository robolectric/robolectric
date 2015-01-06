#!/bin/bash

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

# Build everything
cd "$PROJECT"; mvn clean -Pdist
cd "$PROJECT"; mvn clean install -DskipTests

# Build older shadow packages
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-15
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-16
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-17
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-18
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean velocity:velocity install -Pandroid-19

# Build everything with tests (tests require the shadows)
cd "$PROJECT"; mvn test
