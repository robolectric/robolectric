<a name="README">[<img src="https://rawgithub.com/robolectric/robolectric/master/images/robolectric-horizontal.png"/>](http://robolectric.org)</a>

[![Build Status](https://github.com/robolectric/robolectric/actions/workflows/tests.yml/badge.svg)](https://github.com/robolectric/robolectric/actions?query=workflow%3Atests)
[![GitHub release](https://img.shields.io/github/release/robolectric/robolectric.svg?maxAge=60)](https://github.com/robolectric/robolectric/releases)

Robolectric is the industry-standard unit testing framework for Android. With Robolectric, your tests run in a simulated Android environment inside a JVM, without the overhead and flakiness of an emulator. Robolectric tests routinely run 10x faster than those on cold-started emulators.

Robolectric supports running unit tests for *17* different versions of Android, ranging from Jelly Bean (API level 16) to TIRAMISU (API level 33).

## Usage

Here's an example of a simple test written using Robolectric:

```java
@RunWith(AndroidJUnit4.class)
public class MyActivityTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() {
    Activity activity = Robolectric.setupActivity(MyActivity.class);

    Button button = (Button) activity.findViewById(R.id.press_me_button);
    TextView results = (TextView) activity.findViewById(R.id.results_text_view);

    button.performClick();
    assertThat(results.getText().toString(), equalTo("Testing Android Rocks!"));
  }
}
```

For more information about how to install and use Robolectric on your project, extend its functionality, and join the community of contributors, please visit [http://robolectric.org](http://robolectric.org).

## Install

### Starting a New Project

If you'd like to start a new project with Robolectric tests you can refer to `deckard` (for either [maven](http://github.com/robolectric/deckard-maven) or [gradle](http://github.com/robolectric/deckard-gradle)) as a guide to setting up both Android and Robolectric on your machine.

#### build.gradle:

```groovy
testImplementation "junit:junit:4.13.2"
testImplementation "org.robolectric:robolectric:4.9"
```

## Building And Contributing

Robolectric is built using Gradle. Both IntelliJ and Android Studio can import the top-level `build.gradle` file and will automatically generate their project files from it.

### Prerequisites

Those software configurations are recommended and tested.

- JDK 11. Gradle JVM should be set to Java 11.
  - For command line, make sure the environment variable `JAVA_HOME` is correctly point to JDK11, or set the build environment by [Gradle CLI option](https://docs.gradle.org/current/userguide/command_line_interface.html#sec:environment_options) `-Dorg.gradle.java.home="YourJdkHomePath"` or by [Gradle Properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) `org.gradle.java.home=YourJdkHomePath`.
  - For both IntelliJ and Android Studio, see _Settings/Preferences | Build, Execution, Deployment | Build Tools | Gradle_.
- Ninja 1.10.2+. Check it by `ninja --version`.
- CMake 3.22.1+. Check it by `cmake --version`.
- GCC 7.5.0+ on Linux or Apple clang 12.0.0+ on macOS. Check it by `gcc --version`.

See [Building Robolectric](http://robolectric.org/building-robolectric/) for more details about setting up a build environment for Robolectric.

### Building

Robolectric supports running tests against multiple Android API levels. The work it must do to support each API level is slightly different, so its shadows are built separately for each. To build shadows for every API version, run:

    ./gradlew clean assemble testClasses --parallel

### Testing

Run tests for all API levels:

> The fully tests could consume more than 16G memory(total of physical and virtual memory).

    ./gradlew test --parallel

Run tests for part of supported API levels, e.g. run tests for API level 26, 27, 28:

    ./gradlew test --parallel -Drobolectric.enabledSdks=26,27,28

Run compatibility test suites on opening Emulator:

    ./gradlew connectedCheck

### Using Snapshots

If you would like to live on the bleeding edge, you can try running against a snapshot build. Keep in mind that snapshots represent the most recent changes on master and may contain bugs.

#### build.gradle:

```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
    testImplementation "org.robolectric:robolectric:4.9-SNAPSHOT"
}
```
