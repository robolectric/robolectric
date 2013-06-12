<a name="README">[Robolectric](http://pivotal.github.com/robolectric/index.html)</a>
=======

**An Android Testing Framework**

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the pom.xml file and will automatically generate their project files from it.

For more information about how to use Robolectric on your project, extend its functionality, and join the community of
contributors, please see: [http://pivotal.github.com/robolectric/index.html](http://pivotal.github.com/robolectric/index.html)

[![Build Status](https://secure.travis-ci.org/robolectric/robolectric.png?branch=master)](http://travis-ci.org/robolectric/robolectric)

http://ci.robolectric.org


### Known compile issues
If your build fails because the Google Maps dependency is missing then download the Google APIs for Android 4.1
using the Android SDK Manager and run:

```
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=16_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-16/libs/maps.jar"
```
