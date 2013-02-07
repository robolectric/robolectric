---
layout: default
title: Help and Resources
---

## Who's Using Robolectric?
* [Pivotal Labs](http://pivotallabs.com/ "Pivotal Labs: Home")
* [Xtreme Labs Inc.](http://www.xtremelabs.com/ "Mobile App Development | Blackberry Apps| iPhone Apps | Xtreme Labs Inc.")
* [Square](https://squareup.com/)
* [Path](http://www.path.com/ "Path")
* [Zoodles](http://www.zoodles.com/home/marketing "Zoodles: A safe Kid Mode&#153; for every device")
* [SoundCloud](https://market.android.com/details?id=com.soundcloud.android)
* [Found](http://beta.getfoundapp.com/ "Found &ndash; See where your friends are going.")
* [Frogtek](http://frogtek.org/ "Frogtek")
* [NASA Trained Monkeys](http://www.nasatrainedmonkeys.com/ "NASA Trained Monkeys")
* [Zauber](http://www.zaubersoftware.com/en/home/ "Zauber | Software Development Outsourcing")
* [RoboGuice](http://code.google.com/p/roboguice/ "roboguice - Google Guice on Android - Google Project Hosting")
* [Robolectric Sample](https://github.com/pivotal/RobolectricSample)
* [Android IntelliJ Starter](https://github.com/pivotal/AndroidIntelliJStarter)
* [Android in Practice](http://code.google.com/p/android-in-practice/ "android-in-practice -Source code and demo apps for the Manning book &quot;Android in Practice&quot; - Google Project Hosting")

## Presentation: TDD Android Applications with Robolectric
Pivotal Labs developers have given this presentation several times.

<div style="width:425px" id="__ss_8857513"><strong style="display:block;margin:12px 0 4px"><a href="http://www.slideshare.net/joemoore1/tdd-android-applications-with-robolectric" title="TDD Android Applications with Robolectric">TDD Android Applications with Robolectric</a></strong><object id="__sse8857513" width="425" height="355"><param name="movie" value="http://static.slidesharecdn.com/swf/ssplayer2.swf?doc=tddandroidwithrobolectric-110815140800-phpapp01&amp;stripped_title=tdd-android-applications-with-robolectric&amp;userName=joemoore1" /><param name="allowFullScreen" value="true"/><param name="allowScriptAccess" value="always"/><embed name="__sse8857513" src="http://static.slidesharecdn.com/swf/ssplayer2.swf?doc=tddandroidwithrobolectric-110815140800-phpapp01&amp;stripped_title=tdd-android-applications-with-robolectric&amp;userName=joemoore1" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="425" height="355"></embed></object><div style="padding:5px 0 12px">View more <a href="http://www.slideshare.net/">presentations</a> from <a href="http://www.slideshare.net/joemoore1">Joseph Moore</a>.</div></div>

-----

## Troubleshooting

### java.lang.RuntimeException: Stub!

* Make sure that robolectric and its dependencies appear before the Android API jars in the classpath.

### WARNING: Unable to find path to Android SDK
Robolectric cannot find your Android SDK. Try the following: 

* Set the `sdk.dir` in `local.properties` by running the following: 

         $ android update project -p .

* Set `ANDROID_HOME` environment variable. You can put this in `.bash_profile` for example.

         export ANDROID_HOME=/path/to/android/sdk

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

