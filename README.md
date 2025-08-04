<a name="README">[<img src="https://rawgithub.com/robolectric/robolectric/master/images/robolectric-horizontal.png"/>](https://robolectric.org)</a>

[![Build Status](https://github.com/robolectric/robolectric/actions/workflows/tests.yml/badge.svg)](https://github.com/robolectric/robolectric/actions?query=workflow%3Atests)
[![GitHub release](https://img.shields.io/github/release/robolectric/robolectric.svg?maxAge=60)](https://github.com/robolectric/robolectric/releases)

Robolectric is the industry-standard unit testing framework for Android. With Robolectric, your tests run in a simulated Android environment inside a JVM, without the overhead and flakiness of an emulator. Robolectric tests routinely run 10x faster than those on cold-started emulators.

Robolectric supports running unit tests for *15* different versions of Android, ranging from Lollipop (API level 21) to V (API level 35).

## Usage

To use Robolectric in your project, simply add the necessary dependencies to your module's `build.gradle`/`build.gradle.kts` file:

```groovy
testImplementation("junit:junit:4.13.2")
testImplementation("org.robolectric:robolectric:4.15.1")
testImplementation("androidx.test.ext:junit:1.2.1")
```

Then you can write your tests using Robolectric, like the following example:

```java
@RunWith(AndroidJUnit4.class)
public class MyActivityTest {
  @Test
  public void clickingButton_shouldChangeMessage() {
    try (ActivityController<MyActivity> controller = Robolectric.buildActivity(MyActivity.class)) {
      controller.setup(); // Moves the Activity to the RESUMED state
      MyActivity activity = controller.get();

      activity.findViewById(R.id.button).performClick();
      assertEquals(((TextView) activity.findViewById(R.id.text)).getText(), "Robolectric Rocks!");
    }
  }
}
```

For more information about how to install and use Robolectric in your project, extend its functionality, and join the community of contributors, you can visit [robolectric.org](https://robolectric.org).

## Building and Contributing

Robolectric is built using Gradle. Both Android Studio and IntelliJ can import the top-level `build.gradle.kts` file and will automatically generate their project files from it.

To get Robolectric up and running on your machine, check out
[this guide](https://robolectric.org/building-robolectric/).

To get a high-level overview of Robolectric's architecture, check out
[robolectric.org](https://robolectric.org/architecture).

## Development model

Robolectric is actively developed in several locations. The primary location is
this GitHub repository, which is considered the *source-of-truth* for
Robolectric code. It is where contributions from the broader Android developer
community occur. There is also an active development tree of Robolectric
internally at Google, where contributions from first-party Android developers
occur. By having a development tree of Robolectric internally at Google, it
enables first-party Android developers to more efficiently make contributions
to Robolectric. This tree is synced directly to the [`google`
branch](https://github.com/robolectric/robolectric/tree/google) every
time a change occurs using the [`Copybara`](https://github.com/google/copybara)
code sync tool. Bidirectional merges of this branch and the
[`master`](https://github.com/robolectric/robolectric/tree/master) branch occur
regularly.

Robolectric also has usage in the Android platform via the
[external/robolectric](https://cs.android.com/android/platform/superproject/main/+/main:external/robolectric/)
repo project. Contributions to this source tree are typically related to new
SDK support and evolving platform APIs. Changes from this branch are upstreamed
to the internal Robolectric tree at Google, which eventually propagate to the
GitHub branches.

Although complex, this distributed development model enables Android developers
in different environments to use and contribute to Robolectric, while allowing
changes to eventually make their way to public Robolectric releases.

## Using Snapshots

If you would like to live on the bleeding edge, you can try running against a snapshot build. Keep in mind that snapshots represent the most recent changes on the `master` and may contain bugs.

### `build.gradle`

```groovy
repositories {
    maven { url "https://central.sonatype.com/repository/maven-snapshots/" }
}

dependencies {
    testImplementation "org.robolectric:robolectric:4.16-SNAPSHOT"
}
```
