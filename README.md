# Robolectric

**Android Unit Testing Framework**

![robolectric](http://robolectric.org/images/robolectric.png)

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric) 

http://ci.robolectric.org

## Install

You can install Robolectric for your project by adding the following to your pom.xml:

```xml
<dependency>
	<groupId>org.robolectric</groupId>
   <artifactId>robolectric</artifactId>
   <version>2.1.1/version>
   <scope>test</scope>
</dependency>
```

### Dependencies

#### Android Maven Plugin

Your project should be using the [Android Maven Plugin](https://code.google.com/p/maven-android-plugin/).

#### Google API Jars

Robolectric requires the Google APIs for Android 4.1. You can download these onto your development machine use the Android SDK tools and then run the following to install them to your local Maven repository:

```shell
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=16_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-16/libs/maps.jar"
```

You will need to either replace or have ANDROID_HOME set to your local Android SDK for Maven to be able to install the jar.

## Usage

Here's an example of a simple Activity test using Robolectric:

```java
// Test class for MyActivity
@RunWith(RobolectricTestRunner.class)
public class MyActivityTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() throws Exception {
    Activity activity = Robolectric.buildActivity(MyActivity.class).create().get();

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

## Building And Contributing

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the pom.xml file and will automatically generate their project files from it.

Guides on to extending Robolectric can be found [here](http://robolectric.org/extending.html) and the contributor guidlines can be found [here](http://robolectric.org/contributor_guidelines.html).
