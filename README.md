<a name="README">[Robolectric](http://pivotal.github.com/robolectric/index.html)</a>
=======

**An Android Testing Framework**

Robolectric can be built using either Maven or Ant. Both Eclipse (with the M2Eclipse plug-in) and
IntelliJ can import the pom.xml file and will automatically generate their project files from it.

For more information about how to use Robolectric on your project, extend its functionality, and join the community of
contributors, please see: [http://pivotal.github.com/robolectric/index.html](http://pivotal.github.com/robolectric/index.html)

[![Build Status](https://secure.travis-ci.org/pivotal/robolectric.png?branch=master)](http://travis-ci.org/pivotal/robolectric)

http://ci.robolectric.org


### Known compile issues
If your build fails because of maps.jar is missing then install all the android extra and run:

```
git clone https://github.com/mosabua/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer/
maven install -P <ANDROID_VERSION>
```



