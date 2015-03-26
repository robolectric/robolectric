#!/bin/bash
#
# Build Robolectric including shadows for all API levels.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

echo "Cleaning dist directories..."
cd "$PROJECT"; mvn -P dist clean

echo "Building Robolectric (without tests)..."
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

echo "Building Robolectric and API 21 (with tests)..."
cd "$PROJECT"; mvn -P android-latest test
