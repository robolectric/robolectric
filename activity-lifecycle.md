---
layout: default
title: Driving Activities Through Their Lifecycle
---

# Back in my day...

Prior to Robolectric 2.2, most tests created Activities by calling constructors directly, (`new MyActivity()`) and then manually calling lifecycle methods such as `onCreate()`. Also widely used were a set of methods in `ShadowActivity` (for instance `ShadowActivity.callOnCreate()`) that are precursors to `ActivityController`.

It was a mess. The `ActivityController` is a Robolectric API that changes all of this. Its goal is to mimic how Android creates your Activities and drives them through their lifecycle.

`ActivityController` is a fluent API that was introduced in Robolectric 2.0 and is now required in 2.2. In addition to calling methods like `onCreate()`, it ensures that the internal state of the Activity is consistent with the lifecycle. This includes attaching the Activity to the Window and making system services like the `LayoutInflater` available.

__The old methods for driving Activity lifecycle no longer work (or have been removed) in Robolectric 2.2.__

# What do I do now?

You don't generally create an `ActivityController` directly. Use `Robolectric.buildActivity()` to get started. For the most basic of tests where you simply need an initialized Activity, you can often get away with the following line:

```java
Activity activity = Robolectric.buildActivity(MyAwesomeActivity.class).create().get();
```

This will create a new instance of `MyAwesomeActivity` and call through the life cycle to `onCreate()`.

Want to check that something happens during `onResume()` but not `onCreate()`? Easy!

```java
ActivityController activityController = Robolectric.buildActivity(MyAwesomeActivity.class).create().start();
Activity activity = activityController.get();
// assert that something hasn't happened
activityController.resume();
// assert it happened!
```

Similar methods are included for `start()`, `pause()`, `stop()`, and `destroy()`. So, if you want to test the full creation lifecycle:

```java
Activity readyToGoActivity = Robolectric.buildActivity(MyAwesomeActivity.class)
    .create()
    .start()
    .resume()
    .visible()
    .get();
```

You can simulate starting the Activity with an intent:

```java
Intent intent; // Hey! you should assign me to something.
Activity activityWithIntent = Robolectric.buildActivity(MyAwesomeActivity.class)
	.withIntent(intent)
    .create()
	.get();
```

... or restore saved instance state:

```java
Bundle savedInstanceState; //  Something seems missing here...
Activity activityWithIntent = Robolectric.buildActivity(MyAwesomeActivity.class)
    .create()
	.restoreInstanceState(savedInstanceState)
	.get();
```

Check out the [`ActivityController` Java Docs](/javadoc/org/robolectric/util/ActivityController.html) to see more public methods available for your testing needs.


# Wait, What's This `visible()` Nonsense?

Turns out that in a real Android app, the view hierarchy of an `Activity` is not attached to the `Window` until sometime after `onCreate()` is called. Until this happens, the `Activity`'s views do not report as visible. This means you can't click on them (amongst other unexpected behavior). The `Activity`'s hierarchy is attached to the `Window` on a device or emulator *after* `onPostResume()` on the `Activity`. Rather than make assumptions about when the visibility should be updated, Robolectric puts the power in the developer's hands when writing tests.

So when do you call it? Whenever you're interacting with the views inside the `Activity`. Methods like `Robolectric.clickOn()` require that the view is visible and properly attached in order to function. You should call `visible()` after `create()`.