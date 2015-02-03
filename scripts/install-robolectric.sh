#!/bin/bash
#
# Build Robolectric including shadows for all API levels.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

echo "Cleaning dist directories..."
cd "$PROJECT"; mvn clean -Pdist

echo "Building Robolectric (without tests)..."
cd "$PROJECT"; mvn clean install -DskipTests

echo "Building shadows for API 16..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean install -Pandroid-16

echo "Building shadows for API 17..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean install -Pandroid-17

echo "Building shadows for API 18..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean install -Pandroid-18

echo "Building shadows for API 19..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn clean install -Pandroid-19

echo "Building Robolectric (with tests)..."
cd "$PROJECT"; mvn test
