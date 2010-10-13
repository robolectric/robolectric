---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

## Sample Application
	
A sample app that uses Robolectric can be found at [http://github.com/pivotal/RobolectricSample](http://github.com/pivotal/RobolectricSample).

This sample app shows how to layout your project, includes example tests, and a build.xml file for compiling and running tests. For now, the best way to get started is to download this app, and use it to build your app. 

## Test Annotations

Robolectric must have an opportunity to intercept the class loading process of the Android classes to make this all work. This is done by adding the JUnit annotation to your tests. JUnit will defer processing of the Test file to the class defined in the <code>@RunWith(FastAndroidTestRunner.class)</code> annotation.  The <code>FastAndroidTestRunner.class</code> sets up your test to run with Robolectric.

## <code>TestHelper#proxyFor()</code>

Sometimes Android classes define no methods to access the state of the Android objects under test. The <code>TestHelper#proxyFor()</code> methods provide reference to the fake instances representing Android objects, allowing tests to assert on state otherwise not available.

Suppose the application assigns a drawable resource id on an <code>ImageView</code> in layout xml, like this:

<pre>
&lt;ImageView
    android:id="@+id/pivotal_logo"
    android:layout_width="fill_parent"
    android:layout_height="wrap_content"
    android:src="@drawable/pivotallabs_logo"
    android:layout_marginBottom="10dip"
    /&gt; 
</pre>

Android provides no way to access the drawable resource id that was applied to the <code>ImageView</code>. Robolectric's <code>FakeImageView</code> object records the drawable resource id so you can assert on it in test, like this:

<pre>
@Test
public void shouldHaveALogo() throws Exception {
    ImageView pivotalLogo = (ImageView) activity.findViewById(R.id.pivotal_logo);
	FakeImageView fakePivotalLogo = TestHelper.proxyFor(pivotalLogo);
    assertThat(fakePivotalLogo.resourceId, equalTo(R.drawable.pivotallabs_logo));
}
</pre>

## Fake Objects

Robolectric defines many fake objects that give behavior to the stripped classes in the SDK jar. When an Android class's constructor is invoked, a fake object is created if a fake class has been registered. (See <code>FakeHelper#getGenericProxies()</code> for the complete list of fakes Robolectric provides.)

#### Methods

Fake objects implement methods that have the same signature as the Android class. Robolectric will invoke the method on a fake object when a method with the same signature on the Android class is invoked. 

Suppose an application defined the following line of code:
<pre>
  ...
  this.imageView.setImageResource(R.drawable.pivotallabs_logo);
  ...
</pre>

Under test the <code>FakeImageView#setImageResource(int resId)</code> method on the fake instance would be invoked.

#### Constructors

Fake classes in Robolectric may declare constructors 2 ways: 
1. A by declaring a no args constructor  (or the the implicit no args constructor)
2. A constructor that receives the real object that the fake is representing, like this:
<pre>
@Implements(Handler.class)
public class FakeHandler {
  ...
  public FakeHandler(Handler realHandler) {
      this.realHandler = realHandler;
  }
  ...
</pre>
Robolectric will invoke this constructor passing along the 'real' <code>Handler</code> instance so it can be used in the fake implementation.

Once a fake object is instantiated, Robolectric will look for a method named <code>__constructor__</code> that receives the same arguments that the real object's constructor received. 

If the application code were to invoke the Handler constructor which receives a Looper:
<pre>
new Handler(Looper.myLooper());
</pre>
Robolectric would invoke the following <code>__constructor__</code> method that receives a Looper:
<pre>
@Implements(Handler.class)
public class FakeHandler {
  ...
  public void __constructor__(Looper looper) {
    this.looper = looper;
  }
  ...
</pre>








