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
Eclipse will try to use the Android JUnit test runner by default for Android projects, so you will need a separate test
project to run the test with Robolectric, and set up a run configuration to run the tests with the Eclipse JUnit
Launcher.

* Create a new Java project in parallel with your app's project, as your test project. For example, if your app is named
MyApp, you might create a MyAppRobolectricTest project.

* In many cases it will be advantageous to keep the source code for the tests under the same root folder as the source
for the rest of the project. To make this work, use the "Link Source..." button in the Build Path dialog to create a
link from this test project to the source root for the tests under the main project.

* Add the appropriate Android SDK jars to the test project's build path (e.g.
<code>{android sdk root}/platforms/android-8/android.jar</code> and
<code>{android sdk root}/add-ons/addon_google_apis_google_inc_8/libs/maps.jar</code>)

* [Download](http://pivotal.github.com/robolectric/download.html) robolectric-all.jar place it in your test project and
add it to the build path, along with the JUnit library.

* Add your app's project as a project dependency to the build path of your test project.

* Add a new JUnit run/debug launch configuration to run the tests in the test folder. There may be a warning that
multiple launchers are available, make sure to select the Eclipse JUnit Launcher instead of the Android JUnit Launcher.

### In IntelliJ:
* [Download](http://pivotal.github.com/robolectric/download.html) robolectric-all.jar and add it to your other test
library dependencies (such as junit.jar).

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

* Follow the instructions for Eclipse quick start above, but don't download the robolectric-all.jar. Instead, use git to
clone the Robolectric repository:

  <code>git clone git@github.com:pivotal/robolectric.git</code>

(You'll probably want to fork the repo on github first so you can submit pull requests back to us.)

* Add robolectric as an Eclipse project and make your test project depend on it.

* Add the Android SDK jars to the robolectric project also, as above.

### In IntelliJ:

_more details to come..._

