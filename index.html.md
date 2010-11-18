---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

# Robolectric: Test-Drive Your Android Code

Running tests on an Android emulator or device is slow! Building, deploying, and launching the app often takes a minute or more. That's no way to do TDD. There must be a better way.

Wouldn't it be nice to run your Android tests directly from inside your IDE? Perhaps you've tried, and been thwarted by the dreaded <code>'java.lang.RuntimeException: Stub!'</code>?

[Robolectric](http://github.com/pivotal/robolectric) is a unit test framework that de-fangs the Android SDK jar so you can test-drive the development of your Android app.  Tests run inside the JVM on your workstation in seconds. With Robolectric you can write tests like this:

{% highlight java %}
// Test class for MyActivity
@RunWith(RobolectricTestRunner.class)
public class MyActivityTest {
    private Activity activity;
    private Button pressMeButton;
    private TextView results;
    
    @Before
    public void setUp() throws Exception {
        activity = new MyActivity();
        activity.onCreate(null);
        pressMeButton = (Button) activity.findViewById(R.id.press_me_button);
        results = (TextView) activity.findViewById(R.id.results_text_view);
    }

    @Test
    public void shouldUpdateResultsWhenButtonIsClicked() throws Exception {
        pressMeButton.performClick();
        String resultsText = results.getText().toString();
        assertThat(resultsText, equalTo("Testing Android Rocks!"));
    }
}
{% endhighlight %}

Robolectric makes this possible by intercepting the loading of the Android classes and rewriting the method bodies. Robolectric re-defines Android methods so they return null (or 0, false, etc.), or if provided Robolectric will forward method calls to shadow Android objects giving the Android SDK behavior. Robolectric provides a large number of shadow objects covering much of what a typical application would need to test-drive the business logic and functionality of your application. Coverage of the SDK is improving every day.

#### View Support

Robolectric handles inflation of views, string resource lookups, etc. Some view attributes (id, visibility enabled, text, checked, and src) are parsed and applied to inflated views. Activity and View <code>#findViewById()</code> methods return Android view objects. Support exists for <code>include</code> and <code>merge</code> tags. These features allow tests to assert on view state.

#### Run Tests Outside of the Emulator

Run your tests on your workstation, or on your Continuous Integration environment. Because tests run on your workstation in a JVM and not in the emulator Android runtime, the dexing, packaging, and installation on the emulator steps are not necessary, allowing you to iterate quickly and refactor your code with confidence.

## No Mocking Frameworks Required

An alternate approach to Robolectric is to use mock frameworks such as [Mockito](http://code.google.com/p/mockito/) to mock out the Android SDK. While this is a valid approach, we find it less useful. The use of mock frameworks can make tests hard to read and understand. Mocking causes tests to become reverse implementations of the production code. 

Robolectric allows a test style that is closer to black box testing, making the tests more effective for refactoring and allowing the tests to focus on the behavior of the application instead of the implementation of Android. You can still use a mocking framework along with Robolectric if you like.

## Contributing

We welcome contributions. Please [fork](http://github.com/pivotal/robolectric) and submit pull requests. Don't forget to include tests!

## Sample Project

Look at the sample project at: https://github.com/pivotal/robolectricsample to see how fast and easy it can be to test
drive the development of Android applications.0

#### Robolectric's current maintainers are:

* [Christian Williams](http://github.com/Xian), Pivotal Labs
* [Tyler Schultz](http://github.com/tylerschultz), Pivotal Labs

## Acknowledgments

* Robolectric (previously known as "droid-sugar") was originally developed by [Christian Williams](http://github.com/Xian) at [XtremeLabs](http://www.xtremelabs.com/). Big thanks to XtremeLabs for their support.
* Considerable contributions have been made by numerous Pivots, including [Ian Fisher](mailto:ifisher@pivotallabs.com), [Phil Goodwin](mailto:phil@pivotallabs.com), [Ryan Richard](mailto:rrichard@pivotallabs.com), [Joe Moore](mailto:joe@pivotallabs.com) and [Harry Ugol](mailto:harry@pivotallabs.com).
* Thanks to [Shane Francis](http://shanefrancis.com/) for the inspired name, and to Pivots [Ofri Afek](mailto:ofri@pivotallabs.com) and [Jessica Miller](mailto:jessica@pivotallabs.com) for our logo!

## Support

__Group email:__ [robolectric@googlegroups.com](mailto:robolectric@googlegroups.com)  
__Samples:__ [http://github.com/pivotal/RobolectricSample](http://github.com/pivotal/RobolectricSample)
