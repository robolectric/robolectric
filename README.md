# Robolectric

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric)

Robolectric is a unit test framework that de-fangs the Android SDK so you can test-drive the development of your Android app.

## Usage

Here's an example of a simple test written using Robolectric:

```java
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

## Install

You can install Robolectric for your project by adding the following to your pom.xml:

```xml
<dependency>
   <groupId>org.robolectric</groupId>
   <artifactId>robolectric</artifactId>
   <version>2.2</version>
   <scope>test</scope>
</dependency>
```

### Dependencies

#### Google API Jars

Robolectric requires the Google APIs for Android (specifically, the maps JAR). To download this onto your development
machine use the Android SDK tools and then run the following to install them to your local Maven repository:

```
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=18_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-18/libs/maps.jar"
```

You will need to either replace or have `ANDROID_HOME` set to your local Android SDK for Maven to be able to install the jar.

## Building And Contributing

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the `pom.xml` file and will automatically generate their project files from it.

Guides on to extending Robolectric can be found [here](http://robolectric.org/extending.html) and the contributor guidlines can be found [here](http://robolectric.org/contributor_guidelines.html).