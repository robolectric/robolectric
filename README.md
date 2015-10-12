<a name="README">[<img src="https://rawgithub.com/robolectric/robolectric/master/images/robolectric-horizontal.png"/>](http://robolectric.org)</a>

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric)

Robolectric is a testing framework that de-fangs the Android SDK so you can test-drive the development of your Android app.

## Usage

Here's an example of a simple test written using Robolectric:

```java
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MyActivityTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() throws Exception {
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

If you'd like to start a new project with Robolectric you can use deckard (for either [maven](http://github.com/robolectric/deckard-maven) or [gradle](http://github.com/robolectric/deckard-gradle)). These project will guide you through setting up both Android and Robolectric on your machine.

### Gradle

```groovy
testCompile "org.robolectric:robolectric:3.0"
```

### Maven

```xml
<dependency>
   <groupId>org.robolectric</groupId>
   <artifactId>robolectric</artifactId>
   <version>3.0</version>
   <scope>test</scope>
</dependency>
```

## Building And Contributing

Robolectric is built using Maven. Both Eclipse (with the M2Eclipse plug-in) and IntelliJ can import the `pom.xml` file and will automatically generate their project files from it. You will need to have portions of the Android SDK available in your local Maven repo in order to build Robolectric.

Mavenize all required dependencies by running:

    ./scripts/install-dependencies.rb

Because Robolectric's shadows are compiled against the Android APIs that they target, you must build the shadows for all API levels before being able to run any of the tests. You can build all of Robolectric by running:

    ./scripts/install-robolectric.sh
    
After doing this once, you can build and test against the specific API level you care about:

    mvn install -P android-18 (for example)

### Using Snapshots

If you would like to live on the bleeding edge, you can try running against a snapshot build. Keep in mind that snapshots represent the most recent changes on master and may contain bugs.

### Gradle

```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
    testCompile "org.robolectric:robolectric:3.1-SNAPSHOT"
}
```

### Maven

```xml
<repository>
  <id>sonatype-snapshpots</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>

<dependency>
   <groupId>org.robolectric</groupId>
   <artifactId>robolectric</artifactId>
   <version>3.1-SNAPSHOT</version>
   <scope>test</scope>
</dependency>
```
