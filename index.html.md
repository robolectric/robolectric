---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

# Robolectric - Making TDD for your Android Application Possible

Have you tried to write unit tests for your Android project and been thwarted by the dreaded 'java.lang.RuntimeException: Stub!'? [Robolectric](http://github.com/pivotal/robolectric) is a unit test framework that de-fangs the Android SDK jar so you can test drive the development of your App.  Dream of writing tests like this?

<pre>
@RunWith(FastAndroidTestRunner.class)
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
    public void shouldUpdateTheResultsWhenTheButtonIsClicked() 
            throws Exception {
        pressMeButton.performClick();
        String resultsText = results.getText().toString();
        assertThat(resultsText, equalTo("Testing Android Rocks!"));
    }
}
</pre>

[Robolectric](http://github.com/pivotal/robolectric) makes this possible by intercepting the loading of the Android classes and rewriting the method bodies. By default the methods defined by the SDK jar return null (or 0, false, etc.) instead of throwing a RuntimeException. The other and more important part of what [Robolectric](http://github.com/pivotal/robolectric) does is to proxy to fake Android objects, giving the Android SDK behavior. [Robolectric](http://github.com/pivotal/robolectric) provides a large number of fake objects covering most of what an application would need to test drive the business logic and functionality of your application. Coverage of the SDK is improving every day.

#### View Support

[Robolectric](http://github.com/pivotal/robolectric) handles inflation of views, string resource lookups, etc. Some view attributes (id, visibility enabled, text, checked, and src) are parsed and applied to inflated views. Activity and View <code>#findViewById()</code> methods return Android view objects. Support exists for <code>include</code> and <code>merge</code> tags. These features allow tests to assert on view state.

#### Run Tests Outside of the Emulator

Run your tests on your workstation, or on your Continuous Integration environment. Because tests run on your workstation in a JVM, (and not in the emulator), the code generation, dexing, packaging, and package installation on the emulator steps are not necessary - allowing you to iterate quickly and refactor your code with confidence.

## No Mocking Frameworks Required

An alternate approach to Robolectric is to use mock frameworks such as [Mockito](http://code.google.com/p/mockito/) to mock out the Android SDK. While this is a valid approach, we find it less useful. The use of mock frameworks can make tests hard to read and understand. Mocking causes tests to become reverse implementations of the production code. 

[Robolectric](http://github.com/pivotal/robolectric) allows a test style that is closer to black box testing, making the tests more effective for refactoring and allowing the tests to focus on the behavior of the application instead of the implementation of Android. 

## Contributing

We welcome contributions. Please [fork](http://github.com/pivotal/robolectric) and submit pull requests!

## Support

__Group email:__ [robolectric@googlegroups.com](mailto:robolectric@googlegroups.com)  
__Samples:__ [http://github.com/pivotal/RobolectricSample](http://github.com/pivota/RobolectricSample)
