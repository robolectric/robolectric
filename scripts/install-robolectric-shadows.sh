#!/bin/bash

set -e

mvn clean

(
set -e
cd robolectric-shadows
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-15  -DskipTests
)
(
set -e
cd robolectric-shadows
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-16  -DskipTests
)
(
set -e
cd robolectric-shadows
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-17  -DskipTests
)
(
set -e
cd robolectric-shadows
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-18  -DskipTests
)
(
set -e
cd robolectric-shadows
mvn clean velocity:velocity javadoc:javadoc source:jar install -Pandroid-19  -DskipTests
)

mvn javadoc:javadoc source:jar install
