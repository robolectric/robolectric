---
layout: default
title: Customizing
---

> # NOTE:
> This page mostly refers to Robolectric 1.x. Things have changed significantly with Robolectric 2.0. See
> [this blog post](http://robolectric.blogspot.com/2013/04/the-test-lifecycle-in-20.html) for more information.

--

# Customizing Robolectric

Many aspects of Robolectric can be configured at runtime by subclassing <code>RobolectricTestRunner</code> to create a
custom test runner.

## Test Setup

### Location of AndroidManifest.xml and resources
By default, Robolectric will look for your <code>AndroidManifest.xml</code> file in the current working directory, and
will look for your resource files in the <code>"res"</code> directory in the same place.

You can override these defaults in your custom test runner by specifying alternate values in the super constructor call:

{% highlight java %}
public class CustomTestRunner extends RobolectricTestRunner {
    public CustomTestRunner(Class testClass) throws InitializationError {
        // defaults to "AndroidManifest.xml", "res" in the current directory
        super(testClass, new File("../other/project-root"));
    }
}
{% endhighlight %}

### Application class
By default, Robolectric will create a new instance of the application class specified in AndroidManifest.xml for each test.

You may override this behavior in your custom test runner by overriding the <code>createApplication</code> method:

{% highlight java %}
public class CustomTestRunner extends RobolectricTestRunner {
    public CustomTestRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

    @Override protected Application createApplication() {
        return new CustomApplication();
    }
}
{% endhighlight %}

### Test setup and tear-down
Robolectric resets some internal state automatically before every test, including:
* stored SharedPreferences
* displayed Toasts and Dialogs
* enqueued Looper work

Robolectric provides hooks for specifying your own code to run before and after every test in every test class that uses
your test runner. This can be especially useful for resetting state between tests, or triggering dependency injection
(e.g. with RoboGuice).

{% highlight java %}
public class CustomTestRunner extends RobolectricTestRunner {
    public CustomTestRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

    @Override public void prepareTest(Object test) {
    }

    @Override public void beforeTest(Method method) {
    }

    @Override public void afterTest(Method method) {
    }
}
{% endhighlight %}

## Custom Shadows

Robolectric provides shadow implementations of many Android classes, but you may find that you want to add your own,
or change the way a shadow works. You can do this by registering your shadow classes in the
<code>beforeTest(Method method)</code> method.

{% highlight java %}
public class CustomTestRunner extends RobolectricTestRunner {
    public CustomTestRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

    @Override public void beforeTest(Method method) {
        Robolectric.bindShadowClass(ShadowBitmapFactory.class);
        Robolectric.bindShadowClass(ShadowDrawable.class);
        Robolectric.bindShadowClass(ShadowGeocoder.class);
    }
}
{% endhighlight %}
