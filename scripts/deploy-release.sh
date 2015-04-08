#!/bin/bash
#
# Deploy a release build to Sonatype.
#

set -e

PROJECT=$(cd $(dirname "$0")/..; pwd)

echo "Building Robolectric..."
cd "$PROJECT"; mvn -P release -D skipTests clean deploy

echo "Building shadows for API 16..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P release,android-16 -D skipTests clean deploy

echo "Building shadows for API 17..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P release,android-17 -D skipTests clean deploy

echo "Building shadows for API 18..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P release,android-18 -D skipTests clean deploy

echo "Building shadows for API 19..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P release,android-19 -D skipTests clean deploy

echo "Building shadows for API 21..."
cd "$PROJECT"/robolectric-shadows/shadows-core; mvn -P release,android-21 -D skipTests clean deploy
