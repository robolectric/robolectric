---
  layout: default
  title: Robolectric Quick Start
---

## Quick Start

If you'd like to quickly get started using Robolectric to test your app, these instructions are for you. We'll show how
to include a pre-built Robolectric jar. While it's possible to make some changes to how Robolectric works in this
configuration, you won't be able to make larger changes or contribute to the Robolectric project itself. This is a good
place to get started even if you want to do more complicated work later.

### In Eclipse:

* Create a test project in parallel with your app's project. For example, if your app is named MyApp, create a MyAppRobolectricTest project, and create a test source tree there.

* Add the appropriate Android SDK jars to the test project's build path (e.g. <code>{android sdk root}/platforms/android-8/android.jar</code> and <code>{android sdk root}/add-ons/addon_google_apis_google_inc_8/libs/maps.jar</code>)

* Download robolectric-all.jar, place it in your test project, and add it to the build path.

* Add a new JUnit run/debug launch configuration to run the tests in the test folder.

### In IntelliJ:
* Download robolectric-all.jar and add it to your other test library dependencies (such as junit.jar).

IntelliJ currently has a [bug](http://youtrack.jetbrains.net/issue/IDEA-60449) (please vote for it!) which causes JUnit
tests within Android projects to run very slowly. To get around this, we place our production and test code into a
non-Android module, which allows the tests to build and run at maximum speed, and declare a dependency to this code from
the main Android module, so we can build the final apk.

_more details to come..._


## Setting up for Robolectric development

If you find that you need to extend or modify Robolectric's simulation of Android or you'd like to contribute to the
project, these instructions will help get you started. You can also track the progress of Robolectric as it evolves from
it's [Tracker page](http://www.pivotaltracker.com/projects/105008)

### In Eclipse:

* Follow the instructions for Eclipse quick start above, but don't download the robolectric-all.jar. Instead, use git to clone the Robolectric repository:

  <code>git clone git@github.com:pivotal/robolectric.git</code>

(You'll probably want to fork the repo on github first so you can submit pull requests back to us.)

* Add robolectric as an Eclipse project and make your test project depend on it.

* Add the Android SDK jars to the robolectric project also, as above.

### In IntelliJ:

_more details to come..._

