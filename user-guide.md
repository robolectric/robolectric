---
layout: default
title: User Guide
---

## Sample Application

A sample app that uses Robolectric can be found at
[http://github.com/pivotal/RobolectricSample](http://github.com/pivotal/RobolectricSample).

This sample app shows how to layout your project, includes example tests, and a build.xml file for compiling and
running tests. For now, the best way to get started is to download this app, and use it to build your app.

## Test Annotations

Robolectric must have an opportunity to intercept the class loading process of the Android classes to make this all
work. This is done by adding the JUnit annotation to your tests. JUnit will defer processing of the Test file to the
class defined in the <code>@RunWith(RobolectricTestRunner.class)</code> annotation. The
<code>RobolectricTestRunner.class</code> sets up your test to run with Robolectric.

## <code>Robolectric.shadowOf()</code>

Sometimes Android classes don't provide methods to access the state of the Android objects under test. The
<code>Robolectric.shadowOf()</code> methods provide reference to the shadow instances representing Android objects,
allowing tests to assert on state otherwise not available.

Suppose the application assigns a drawable resource id on an <code>ImageView</code> in layout xml, like this:

{% highlight xml %}
<ImageView
    android:id="@+id/pivotal_logo"
    android:layout_width="fill_parent"
    android:layout_height="wrap_content"
    android:src="@drawable/pivotallabs_logo"
    android:layout_marginBottom="10dip"
    > 
{% endhighlight %}

Android provides no way to access the drawable resource id that was applied to the <code>ImageView</code>.
Robolectric's <code>ShadowImageView</code> object records the drawable resource id so you can assert on it in test,
like this:

{% highlight java %}
@Test
public void shouldHaveALogo() throws Exception {
    ImageView pivotalLogo = (ImageView) activity.findViewById(R.id.pivotal_logo);
    ShadowImageView shadowPivotalLogo = Robolectric.shadowOf(pivotalLogo);
    assertThat(shadowPivotalLogo.resourceId, equalTo(R.drawable.pivotallabs_logo));
}
{% endhighlight %}

## Shadow Objects

Robolectric defines many shadow objects that give behavior to the stripped classes in the SDK jar. When an Android
class's constructor is invoked, a shadow object is created if a shadow class has been registered.
(See <code>Robolectric.getDefaultShadowClasses()</code> for the complete list of shadows Robolectric provides.)

#### Writing your own Shadow Classes
The library of shadow classes supplied with Robolectric does not cover the entire Android API. Even if it did, some
projects will require behavior that differs from what is in the library. When these situations are encountered
it will be necessary to extend existing or add new shadow classes. Creating new shadow classes is easy. Here is an
outline of the process, details about each step will follow:

- <b>Clone the Robolectric project on GitHub</b>
We very often make Robolectric a sub-module of the project that we are working on in order to make it easier to add
new shadow classes as we need them, but you could also create dependencies between projects or build and copy .jar files
depending on your needs.

- <b>Add tests for your shadow class</b>
They live in the com.extremelabs.robolectric.shadows package under the code/tests folder

- <b>Develop the implementation</b>
Put it in the same package under code/src. There are lots of shadow classes that are already implemented there that can
be used as examples. The most important aspects of writing a shadow class are described below.

- <b>Register your new class with the Robolectric framework</b>
Add it to the list returned by Robolectric.getDefaultShadowClasses() and also add an implementation of
Robolectric.shadowOf(). Just duplicate the examples that are already in the Robolectric class.

If would like to contribute your code to the Robolectric community, we would love to receive your pull requests through
GitHub.

#### Shadow Classes

Shadow classes always need a public no-arg constructor so that the Robolectric framework can instantiate them. They are
associated to the class that they shadow with an @Implements annotation on the class declaration. In general, they
should be implemented as if from scratch, the facilities of the classes they shadow have almost always been removed and
their data members are difficult to access. The methods on a shadow class usually either shadow the methods on the
original class or facilitate testing by setting up return values or providing access to internal state or logged
method calls.

Shadow classes should mimic the production classes' inheritance hierarchy. For example, if you are implementing a shadow
for ViewGroup, ShadowViewGroup, then your shadow class should extend ViewGroup's superclass's shadow,
ShadowView.
{% highlight java %}
  ...
  @Implements(ViewGroup.class)
  public class ShadowViewGroup extends ShadowView {
  ...
{% endhighlight %}

#### Methods

Shadow objects implement methods that have the same signature as the Android class. Robolectric will invoke the method
on a shadow object when a method with the same signature on the Android object is invoked.

Suppose an application defined the following line of code:
{% highlight java %}
  ...
  this.imageView.setImageResource(R.drawable.pivotallabs_logo);
  ...
{% endhighlight %}

Under test the <code>ShadowImageView#setImageResource(int resId)</code> method on the shadow instance would be invoked.

Shadow methods must be marked with the <code>@Implementation</code> annotation. Robolectric includes a lint test to help
ensure this is done correctly.

It is important shadow methods are implemented on the corresponding shadow of the class in which they were
originally defined. Otherwise Robolectric's lookup mechanism will not find them (even if they have been declared on a
shadow subclass.) For example, the method <code>setEnabled()</code> is defined on View. If a <code>setEnabled()</code>
method is defined on <code>ShadowViewGroup</code> instead of <code>ShadowView</code> then it will not be found at run
time even when <code>setEnabled()</code> is called on an instance of <code>ViewGroup</code>.

#### Shadowing Constructors

Once a shadow object is instantiated, Robolectric will look for a method named <code>__constructor__</code> which has
the same arguments as the constructor that was invoked on the real object.

For instance, if the application code were to invoke the TextView constructor which receives a Context:
{% highlight java %}
new TextView(context);
{% endhighlight %}
Robolectric would invoke the following <code>__constructor__</code> method that receives a Context:
{% highlight java %}
@Implements(TextView.class)
public class ShadowTextView {
  ...
  public void __constructor__(Context context) {
    this.context = context;
  }
  ...
{% endhighlight %}


#### Getting access to the real instance

Sometimes shadow classes may want to refer to the object they are shadowing, e.g. to manipulate fields. A shadow class
can accomplish this by declaring a field annotated @RealObject:
{% highlight java %}
@Implements(Point.class)
public class ShadowPoint {
    @RealObject private Point realPoint;
    ...
    public void __constructor__(int x, int y) {
        realPoint.x = x;
        realPoint.y = y;
    }
}
{% endhighlight %}
Robolectric will set realPoint to the actual instance of <code>Point</code> before invoking any other methods.

It is important to note that methods called on the real object will still be intercepted and redirected by Robolectric.
This does not often matter in test code, but it has important implications for Shadow class implementors. Since the
Shadow class inheritance hierarchy does not always mirror that of their associated Android classes, it is sometimes
necessary to make calls through these real objects so that the Robolectric runtime will have the opportunity to route
them to the correct Shadow class based on the actual class of the object. Otherwise methods on Shadows of base classes
would be unable to access methods on the shadows of their subclasses.



