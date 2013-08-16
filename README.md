# Robolectric

![robolectric](http://robolectric.org/images/robolectric.png)

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric) 

http://ci.robolectric.org

## Usage

### Dependencies

Robolectric requires the Google APIs for Android 4.1. You can download these onto your development machine use the Android SDK tools and then run the following:

```
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=16_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-16/libs/maps.jar"
```

You will need to either replace or have ANDROID_HOME set to your local Android SDK for Maven to be able to install the jar.

For more information about how to install and use Robolectric on your project, extend its functionality, and join the community of
contributors, please visit
[http://robolectric.org](http://robolectric.org).

## Building And Contributing

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the pom.xml file and will automatically generate their project files from it.

Guides on to extending Robolectric can be found [here](http://robolectric.org/extending.html) and the contributor guidlines can be found [here](http://robolectric.org/contributor_guidelines.html).
