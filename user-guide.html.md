---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

## Sample Application
	
A sample app that uses Robolectric can be found at [http://github.com/pivotal/RobolectricSample](http://github.com/pivotal/RobolectricSample).

This sample app shows how to layout your project, includes example tests, and a build.xml file for compiling and running tests. 

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
    int actualDrawableId = TestHelper.proxyFor(pivotalLogo).resourceId;
    assertThat(actualDrawableId, equalTo(R.drawable.pivotallabs_logo));
}
</pre>

