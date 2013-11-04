---
layout: default
title: Working with ApkLibs
---

# Configuring ApkLibs

## project.properties

If your project uses any ApkLibs you will have to set these up to work correctly with Robolectric. This
simply requires creating a `project.properties` file for your project that declares the dependencies to
Robolectric.

The `project.properties` file for your project should sit in the root of the project and looks something like this:

```
target=android-<android API level to target>
android.library.reference.1=<relative path to first dependency>
android.library.reference.2=<relative path to second dependency>
```

Robolectric will look for and then parse this file to load dependencies before any tests run.

## Multiple dependency locations

If you're working in an IDE such as IntelliJ and then pushing to a CI that uses Maven you might run into
the problem of a dependency appearing in different places depending on the environment - the IDE will probably compile
to a different directory structure than Maven would. To deal with this
Robolectric will only attempt to load dependencies that exist at run time. This means you can just
list both possible locations for the dependency like so:

```
android.library.reference.1=dev-env-dir/my-awesome-dependency
android.library.reference.2=ci-env-dir/my-awesome-dependency
```