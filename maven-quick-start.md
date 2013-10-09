---
  layout: default
  title: Maven Quick Start
---

## Setup
These days it is simple to set up a bare-bones Android project that uses Maven and Robolectric. Just use [Deckard](http://github.com/robolectric/deckard).

### Deploying .apks
Deckard uses the [maven-android-plugin](http://code.google.com/p/maven-android-plugin/) which makes it easy to add Android to your
Maven project.

### Importing into an IDE
If you're using JetBrains' excellent [IntelliJ IDEA](http://www.jetbrains.com/idea/), just open your
<code>pom.xml</code> as a project, and you're set.

If you're using Eclipse, you can try to get it to work by following the [eclipse quick start instructions](eclipse-quick-start.html).

When running tests from within IntelliJ or Eclipse, be sure to run tests using the JUnit runner and not the Android Test
runner.
