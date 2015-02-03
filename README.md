<a name="README">[<img src="https://rawgithub.com/robolectric/robolectric/master/images/robolectric-horizontal.png"/>](http://robolectric.org)</a>

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric)

Robolectric is a testing framework that de-fangs the Android SDK so you can test-drive the development of your Android app.

## Usage

Here's an example of a simple test written using Robolectric:

```java
@RunWith(RobolectricTestRunner.class)
public class MyActivityTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() throws Exception {
    Activity activity = Robolectric.setupActivity(MyActivity.class);

    Button pressMeButton = (Button) activity.findViewById(R.id.press_me_button);
    TextView results = (TextView) activity.findViewById(R.id.results_text_view);

    pressMeButton.performClick();
    String resultsText = results.getText().toString();
    assertThat(resultsText, equalTo("Testing Android Rocks!"));
  }
}
```

For more information about how to install and use Robolectric on your project, extend its functionality, and join the community of
contributors, please visit
[http://robolectric.org](http://robolectric.org).

## Install

### Starting a new project

If you'd like to start a new project with Robolectric you can use deckard (for either [maven](http://github.com/robolectric/deckard-maven)
or [gradle](http://github.com/robolectric/deckard-gradle)). These project will guide you through setting
up both Android and Robolectric on your machine.

### Gradle

```groovy
testCompile "org.robolectric:robolectric:2.4"
```

### Maven

```xml
<dependency>
   <groupId>org.robolectric</groupId>
   <artifactId>robolectric</artifactId>
   <version>2.4</version>
   <scope>test</scope>
</dependency>
```

Robolectric requires the Google APIs for Android (specifically, the maps JAR) and Android support-v4 library. To download this onto your development machine use the Android SDK tools and then run the following to install them to your local Maven repository (you will need to have the 'Android Support Repository' installed):

```
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=18_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-18/libs/maps.jar"

mvn install:install-file -DgroupId=com.android.support \
  -DartifactId=support-v4 \
  -Dversion=19.0.1 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/extras/android/m2repository/com/android/support/support-v4/19.0.1/support-v4-19.0.1.jar"
```

You will need to either replace or have `ANDROID_HOME` set to your local Android SDK for Maven to be able to install the jar.

## Building And Contributing

Robolectric is built using Maven. Both Eclipse (with the M2Eclipse plug-in) and IntelliJ can import the `pom.xml` file and will automatically generate their project files from it.

Guides on to extending Robolectric can be found [here](http://robolectric.org/extending/) and the contributor guidlines can be found [here](http://robolectric.org/contributor-guidelines/).

### Using SNAPSHOTS

If you would like to live on the bleeding edge, you can try running against a snapshot build. Keep in mind that snapshots represent the most recent changes on master and may contain bugs.

### Gradle

```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
    testCompile "org.robolectric:robolectric:3.0-SNAPSHOT"
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
   <version>3.0-SNAPSHOT</version>
   <scope>test</scope>
</dependency>
```
