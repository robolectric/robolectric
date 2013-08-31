---
layout: default
title: Android Jars
---

## How Robolectric Loads Android Jars

Robolectric has a hard (compile-time) dependency on the full Android jar distributed by google. This is in the pom.xml:


	<dependency>
	    <groupId>com.google.android</groupId>
	    <artifactId>android</artifactId>
	    <version>4.1.1.4</version>
	    <scope>provided</scope>
	</dependency>


This is what Robolectric compiles against, but this jar does not contain all the Android code needed. This is because Robolectric needs to load and run pieces of Android which are internal and are not in this official release.

To load this code, the Robolectric maintainers have checked out the full AOSP and built all the code needed by Robolectric. These built artifacts are stored in remote Maven repositories, including MavenCentral and Sonatype OSS, under groupId "org.robolectric". Maven Central is for the "official" releases of these jars, while Sonatype is for development, before we've released Robolectric major versions.

When Robolectric starts up, it checks your local Maven repository to see if these dependencies are present. If not it downloads these artifacts using Maven libraries. These dependencies are defined in the SdkConfig class.

## Building the Android Artifacts

See [Downloading and Building](http://source.android.com/source/building.html) in the AOSP documentation. When you use the 'repo init' command, make sure to give it the appropriate tag name. For the current Robolectric (2.2-SNAPSHOT as of this writing), use this repo command:

	repo init -u https://android.googlesource.com/platform/manifest -b android-4.1.2_r1

This takes a long, long time to download the entire AOSP source tree.

## Packaging and Uploading the Android Artifacts

Currently, each android artifact ('android-base', 'android-kxml2', 'android-luni', 'android-policy') needs four components bundled together. Currently only the `XYZ-real.jar` component is useful - this is where the actual compiled code lives. The `XYZ.jar`, `XYZ-javadoc.jar`, `XYZ-sources.jar` are all empty. If you need to create these empty jars, just create and empty directory and use

	jar cf empty.jar .

then rename appropriately.

### Sonatype
The built artifacts can be uploaded to the org.robolectric Sonatype by project maintainers by following the instructions at [Deploy Snapshots and Stage Releases](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7a.DeploySnapshotsandStageReleaseswithMaven).

To upload artifacts to Sonatype, you need to sign them with GPG. If you do so you will need to create a GPG keypair and push it to a public keyserver (see the docs, above). Even once you've done this, the Sonatype upload may fail in the signing "rule". Wait 20 minutes or so, it takes a while before the keys are really available.

In addition, for maven to upload the artifacts, you'll need to have Sonatype credentials that can publish to org.robolectric. Maven will look for these in ~/.m2/settings.xml. Here's an example of the settings.xml, with the actual credentials removed. If you are a maintainer and need access, please get in contact with one of the existing maintainers.

	<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
	                        http://maven.apache.org/xsd/settings-1.0.0.xsd">

	    <servers>
	        <server>
	            <id>sonatype-nexus-snapshots</id>
	            <username>username</username>
	            <password>password</password>
	        </server>
	        <server>
	            <id>sonatype-nexus-staging</id>
	            <username>username</username>
	            <password>password</password>
	        </server>
	    </servers>

	    <profiles>
	        <profile>
	            <id>android</id>
	            <properties>
	                <android.sdk.path>
	                    /usr/local/Cellar/android-sdk/r22.0.4
	                </android.sdk.path>
	            </properties>
	        </profile>
	    </profiles>
	    <activeProfiles>
	        <!--make the profile active all the time -->
	        <activeProfile>android</activeProfile>
	    </activeProfiles>
	</settings>