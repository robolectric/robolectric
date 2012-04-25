<a name="README">[Robolectric](http://pivotal.github.com/robolectric/index.html)</a>
=======

**An Android Testing Framework**

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the pom.xml file and will automatically generate their project files from it.

If missing the com.google.android.maps:maps:jar dependency, use the Android SDK android tool and install 2.3 (API level 9), the compatibility package, and the Google APIs for API 9, then use [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer) to put into the local maven repository, using "mvn install -P2.3".

For more information about how to use Robolectric on your project, extend its functionality, and join the community of
contributors, please see: [http://pivotal.github.com/robolectric/index.html](http://pivotal.github.com/robolectric/index.html)
