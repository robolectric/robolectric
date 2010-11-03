---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

## Sample Application
	
A sample app that uses Robolectric can be found at [http://github.com/pivotal/RobolectricSample](http://github.com/pivotal/RobolectricSample).

This sample app shows how to layout your project, includes example tests, and a build.xml file for compiling and running tests. For now, the best way to get started is to download this app, and use it to build your app. 

## Test Annotations

Robolectric must have an opportunity to intercept the class loading process of the Android classes to make this all work. This is done by adding the JUnit annotation to your tests. JUnit will defer processing of the Test file to the class defined in the <code>@RunWith(RobolectricTestRunner.class)</code> annotation. The <code>RobolectricTestRunner.class</code> sets up your test to run with Robolectric.

## <code>Robolectric#shadowOf()</code>

Sometimes Android classes don't provide methods to access the state of the Android objects under test. The <code>Robolectric#shadowOf()</code> methods provide reference to the shadow instances representing Android objects, allowing tests to assert on state otherwise not available.

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

Android provides no way to access the drawable resource id that was applied to the <code>ImageView</code>. Robolectric's <code>ShadowImageView</code> object records the drawable resource id so you can assert on it in test, like this:

{% highlight java %}
@Test
public void shouldHaveALogo() throws Exception {
    ImageView pivotalLogo = (ImageView) activity.findViewById(R.id.pivotal_logo);
	ShadowImageView shadowPivotalLogo = Robolectric.shadowOf(pivotalLogo);
    assertThat(shadowPivotalLogo.resourceId, equalTo(R.drawable.pivotallabs_logo));
}
{% endhighlight %}

## Shadow Objects

Robolectric defines many shadow objects that give behavior to the stripped classes in the SDK jar. When an Android class's constructor is invoked, a shadow object is created if a shadow class has been registered. (See <code>Robolectric#getGenericProxies()</code> for the complete list of shadows Robolectric provides.) A shadow class should always have a no-args constructor.

#### Methods

Shadow objects implement methods that have the same signature as the Android class. Robolectric will invoke the method on a shadow object when a method with the same signature on the Android class is invoked. 

Suppose an application defined the following line of code:
{% highlight java %}
  ...
  this.imageView.setImageResource(R.drawable.pivotallabs_logo);
  ...
{% endhighlight %}

Under test the <code>ShadowImageView#setImageResource(int resId)</code> method on the shadow instance would be invoked.

#### Shadowing Constructors

Once a shadow object is instantiated, Robolectric will look for a method named <code>__constructor__</code> which has the same signature as the constructor which was invoked on the real object.

For instance, if the application code were to invoke the TextView constructor which receives a Context:
{% highlight java %}
new TextView(context);
{% endhighlight %}
Robolectric would invoke the following <code>__constructor__</code> method that receives a Context:
{% highlight java %}
@Implements(TextView.class)
public class TextView {
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



