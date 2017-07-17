# Contributing to Robolectric

## Getting Started

Dependencies:

1. Android SDK with Tools, Extras, and 'Google APIs' for APIs 22 and 23 installed

Set Android enviroment variables:

    export ANDROID_HOME=/path-to-sdk-root
    export PATH=${PATH}:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

Fork and clone the repo:

    git clone git@github.com:username/robolectric.git

Create a feature branch to make your changes:

    git checkout -b my-feature-name

Copy all required Android dependencies into your local Maven repository:

    ./scripts/install-dependencies.rb

Perform a full build of all shadows:

    ./gradlew clean assemble install compileTestJava

## Building and Testing

Robolectric's tests run against the jars that are installed in your local Maven repo. This means that for the tests to pick up your code changes, you must run `mvn install` before running `mvn test`. Running `mvn install` will only build and install shadows for API 21. If your tests run against older versions of Android, you will need to activate a different profile (i.e. `mvn test -P android-19`).

To include the source jar in the build:

    export INCLUDE_SOURCE=1

Similarly with Javadocs:

    export INCLUDE_JAVADOC=1

## Writing Tests

Robolectric is a unit testing framework and it is important that Robolectric itself be very well tested. All classes should have unit test classes. All public methods should have unit tests. Those classes and methods should have their possible states well tested. Pull requests without tests will be sent back to the submitter.

## Code Style

Essentially the IntelliJ default Java style, but with two-space indents.

1. Spaces, not tabs.
2. Two space indent.
3. Curly braces for everything: if, else, etc.
4. One line of white space between methods.
