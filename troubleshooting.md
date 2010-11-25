---
layout: default
title: Troubleshooting
---

## Troubleshooting

--

### java.lang.RuntimeException: Stub!

* Make sure that robolectric and its dependencies appear before the Android API jars in the classpath.

--

### Type com.google.android.maps.MapView not present

<div class="stacktrace">java.lang.TypeNotPresentException: Typecom.google.android.maps.MapView not present
       at sun.reflect.annotation.TypeNotPresentExceptionProxy.generateException(TypeNotPresentExceptionProxy.java:45)
       at sun.reflect.annotation.AnnotationInvocationHandler.invoke(AnnotationInvocationHandler.java:74)
       at $Proxy6.value(Unknown Source)
       at com.xtremelabs.robolectric.Robolectric.bindShadowClass(Robolectric.java:67)
       at com.xtremelabs.robolectric.Robolectric.bindShadowClasses(Robolectric.java:76)
       at com.xtremelabs.robolectric.Robolectric.bindDefaultShadowClasses(Robolectric.java:71)
       at com.xtremelabs.robolectric.RobolectricTestRunner.setupApplicationState(RobolectricTestRunner.java:231)
       at com.xtremelabs.robolectric.RobolectricTestRunner.internalBeforeTest(RobolectricTestRunner.java:177)
Caused by: java.lang.ClassNotFoundException: caught an exception while obtaining a class file for com.google.android.maps.MapView
       at javassist.Loader.findClass(Loader.java:359)
       at com.xtremelabs.robolectric.RobolectricClassLoader.findClass(RobolectricClassLoader.java:60)
       at javassist.Loader.loadClass(Loader.java:311)
       at java.lang.ClassLoader.loadClass(ClassLoader.java:266)
       at com.xtremelabs.robolectric.RobolectricClassLoader.loadClass(RobolectricClassLoader.java:37)
       at java.lang.Class.forName0(Native Method)
       at java.lang.Class.forName(Class.java:264)
...
       at com.xtremelabs.robolectric.Robolectric.bindShadowClass(Robolectric.java:63)       ... 19 more
Caused by: javassist.NotFoundException:android.widget.ZoomButtonsController
       at javassist.ClassPool.get(ClassPool.java:436)
...
       at com.xtremelabs.robolectric.AndroidTranslator.describe(AndroidTranslator.java:208)
       at com.xtremelabs.robolectric.AndroidTranslator.fixMethod(AndroidTranslator.java:212)
       at com.xtremelabs.robolectric.AndroidTranslator.fixMethods(AndroidTranslator.java:196)
       at com.xtremelabs.robolectric.AndroidTranslator.onLoad(AndroidTranslator.java:80)
       at javassist.Loader.findClass(Loader.java:340)       ... 37 more
</div>

1. Make sure you have the Google Maps API jar in your build path.
2. Even if you're building against an earlier version of the API, link Robolectric to version 7 or higher.

