#!/bin/bash

# This script takes org.apache.http.legacy.jar (generated from AOSP sources
# using `m org.apache.http.legacy`), and prepares a bundle for upload to Maven
# Central Portal. This artifact is used for httpclient shadows tests.

# Exit on subcommand error or an undefined variable. Also print commands.
set -eux

GROUP_ID="org.robolectric"
ARTIFACT_ID="legacy-apache-http-client-for-robolectric-testing"
VERSION="1.0"

GPG_KEY_ID="5767F9CDE920750621875079A40E24B5B408DBD5"

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <path/to/org.apache.http.legacy.jar>" >&2
    echo "Error: Please provide exactly one argument: the path to org.apache.http.legacy.jar" >&2
    exit 1
fi

if [ ! -f "$1" ]; then
  echo "Error: File not found at '$1'" >&2
  exit 1
fi

ORIGINAL_JAR_PATH=$(realpath "$1")
BASE_FILENAME="${ARTIFACT_ID}-${VERSION}"

TMP_DIR=$(mktemp -d -t maven-staging-XXXXXX)
echo "Created temporary staging directory at: ${TMP_DIR}"

# 3. Create the nested directory structure required by Maven
ARTIFACT_DIR="${TMP_DIR}/org/robolectric/${ARTIFACT_ID}/${VERSION}"
mkdir -p "${ARTIFACT_DIR}"

cd "${ARTIFACT_DIR}"

cp "${ORIGINAL_JAR_PATH}" "${BASE_FILENAME}.jar"

cat <<EOF > "${BASE_FILENAME}.pom"
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>${GROUP_ID}</groupId>
    <artifactId>${ARTIFACT_ID}</artifactId>
    <version>${VERSION}</version>
    <packaging>jar</packaging>
    <name>${ARTIFACT_ID}</name>
    <description>Legacy Apache HTTP Client for Robolectric testing purposes.</description>
    <url>http://robolectric.org</url>
    <licenses>
        <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>https://android.googlesource.com/platform/external/apache-http/+/refs/heads/android12-dev/</url>
        <connection>git://android.googlesource.com/platform/external/apache-http/+/refs/heads/android12-dev/</connection>
    </scm>
    <developers>
        <developer>
            <name>The Android Open Source Projects</name>
        </developer>
    </developers>
</project>
EOF

echo "Generating empty sources and javadoc JARs"
EMPTY_DIR=$(mktemp -d)
jar cf "${BASE_FILENAME}-sources.jar" -C "${EMPTY_DIR}" .
jar cf "${BASE_FILENAME}-javadoc.jar" -C "${EMPTY_DIR}" .
rm -rf "${EMPTY_DIR}"

echo "Signing files with GPG key: ${GPG_KEY_ID}"
for file in *.jar *.pom; do
  gpg --batch --yes --detach-sign --armor -u "${GPG_KEY_ID}" "${file}"
done

echo "Generating checksums (md5, sha1, sha256, sha512)"
for file in *; do
  if [ -f "${file}" ]; then
    md5sum "${file}" | cut -d ' ' -f 1 > "${file}.md5"
    sha1sum "${file}" | cut -d ' ' -f 1 > "${file}.sha1"
    sha256sum "${file}" | cut -d ' ' -f 1 > "${file}.sha256"
    sha512sum "${file}" | cut -d ' ' -f 1 > "${file}.sha512"
  fi
done

echo "Creating zip bundle"
cd "${TMP_DIR}"
zip -r "${ARTIFACT_ID}-${VERSION}-bundle.zip" "org"

echo "Success!"
echo "The final bundle zip file is located here:"
echo "${TMP_DIR}/${ARTIFACT_ID}-${VERSION}-bundle.zip"
