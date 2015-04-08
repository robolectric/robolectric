#!/bin/bash
#
# Install Robolectric into the local Maven repository.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

echo "Building Robolectric..."
cd "$PROJECT"; mvn -D skipTests clean install

echo "Building shadows for API 16..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P android-16 clean install

echo "Building shadows for API 17..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P android-17 clean install

echo "Building shadows for API 18..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P android-18 clean install

echo "Building shadows for API 19..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P android-19 clean install

echo "Building shadows for API 21..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P android-21 clean install

echo "Running Tests..."
cd "$PROJECT"; mvn test
