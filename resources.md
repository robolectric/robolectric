---
layout: default
title: Help and Resources
---

## Who's Using Robolectric?
* [Pivotal Labs](http://pivotallabs.com/ "Pivotal Labs: Home")
* [Square](https://squareup.com/)
* [Path](http://www.path.com/ "Path")
* [Zoodles](http://www.zoodles.com/home/marketing "Zoodles: A safe Kid Mode&#153; for every device")
* [SoundCloud](https://market.android.com/details?id=com.soundcloud.android)
* [Found](http://beta.getfoundapp.com/ "Found &ndash; See where your friends are going.")
* [Frogtek](http://frogtek.org/ "Frogtek")
* [NASA Trained Monkeys](http://www.nasatrainedmonkeys.com/ "NASA Trained Monkeys")
* [Zauber](http://www.zaubersoftware.com/en/home/ "Zauber | Software Development Outsourcing")
* [RoboGuice](http://code.google.com/p/roboguice/ "roboguice - Google Guice on Android - Google Project Hosting")
* [Android in Practice](http://code.google.com/p/android-in-practice/ "android-in-practice -Source code and demo apps for the Manning book &quot;Android in Practice&quot; - Google Project Hosting")
* [Xtreme Labs Inc.](http://www.xtremelabs.com/ "Mobile App Development | Blackberry Apps| iPhone Apps | Xtreme Labs Inc.")
* [Robolectric Sample](https://github.com/robolectric/RobolectricSample)
* [Android IntelliJ Starter](https://github.com/pivotal/AndroidIntelliJStarter)

## Presentation: TDD Android Applications with Robolectric
Pivotal Labs developers have given this presentation several times.

<div style="width:425px" id="__ss_8857513"><strong style="display:block;margin:12px 0 4px"><a href="http://www.slideshare.net/joemoore1/tdd-android-applications-with-robolectric" title="TDD Android Applications with Robolectric">TDD Android Applications with Robolectric</a></strong><object id="__sse8857513" width="425" height="355"><param name="movie" value="http://static.slidesharecdn.com/swf/ssplayer2.swf?doc=tddandroidwithrobolectric-110815140800-phpapp01&amp;stripped_title=tdd-android-applications-with-robolectric&amp;userName=joemoore1" /><param name="allowFullScreen" value="true"/><param name="allowScriptAccess" value="always"/><embed name="__sse8857513" src="http://static.slidesharecdn.com/swf/ssplayer2.swf?doc=tddandroidwithrobolectric-110815140800-phpapp01&amp;stripped_title=tdd-android-applications-with-robolectric&amp;userName=joemoore1" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="425" height="355"></embed></object><div style="padding:5px 0 12px">View more <a href="http://www.slideshare.net/">presentations</a> from <a href="http://www.slideshare.net/joemoore1">Joseph Moore</a>.</div></div>

-----

## Troubleshooting

### java.lang.RuntimeException: Stub!

* Make sure you've put `@RunWith(RobolectricTestRunner.class)` at the top of your test class.
* Make sure that robolectric and its dependencies (including JUnit) appear before the Android API jars in the classpath.

----

### Could not resolve dependencies for project: Could not find artifact com.google.android.maps:maps:jar:18_r3 in central (http://repo1.maven.org/maven2)

The jerk lawyers at Google won't allow the Google maps add-on library stubs to be uploaded to Maven Central. You need to manually install them yourself.

Make sure you've got the Android Google SDK listed [here](https://github.com/robolectric/robolectric/blob/master/pom.xml#L95) (look for `com.google.android.maps`; currently it's `18_r3`) downloaded, then do this:

    ./script/install-maps-jar.sh

Or, just make sure you have the latest Google Maps API jar and do this:

    cd $ANDROID_HOME
    ls -1d add-ons/addon-google_apis-google-* | sort | tail -1 |
        xargs -I% mvn install:install-file -DgroupId=com.google.android.maps -DartifactId=maps -Dversion=16_r3 -Dpackaging=jar -Dfile=%/libs/maps.jar

----

### Unable to find Android SDK
> **NOTE: this problem no longer happens in Robolectric 2.0. You oughta upgrade!**
Robolectric cannot find your Android SDK. You can tell Robolectric how to find your SDK root in several ways:

##### `local.properties` file
Set the `sdk.dir` in a `local.properties` file by running the following in your project's root dir:

    $ android update project -p .

 Setting up a `local.properties` file is a solution that will work for most IDEs since you don't need to worry about getting environment variables passed around.

##### `ANDROID_HOME` environment variable
Set `ANDROID_HOME` environment variable. You can put this in your `.bash_profile` for example. You may need to do some [extra work](http://www.dowdandassociates.com/content/howto-set-environment-variable-mac-os-x-etclaunchdconf) to get your IDE to pick it up.

    export ANDROID_HOME=/path/to/android/sdk

##### `android.sdk.path` system property
Set the Java system property `android.sdk.path`, e.g. by putting `-Dandroid.sdk.path=/path/to/android/sdk` on the command line.

##### `which android`
As a last resort, Robolectric will try running `which android` to find the executable on your path. Add the SDK tools to your path:

    PATH=/path/to/android/sdk/tools:$PATH

### Type com.google.android.maps.MapView not present

<div class="stacktrace">java.lang.TypeNotPresentException: Typecom.google.android.maps.MapView not present
       at org.robolectric.Robolectric.bindShadowClass(Robolectric.java:67)
Caused by: java.lang.ClassNotFoundException: caught an exception while obtaining a class file for com.google.android.maps.MapView
...
</div>

1. Make sure you have the Google Maps API jar in your build path.
2. Even if you're building against an earlier version of the API, link Robolectric to version 7 or higher.
