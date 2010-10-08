---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

# Making TDD for your Android Application Possible

Have you tried to write unit tests for your Android project and been thwarted by the dreaded 'java.lang.RuntimeException: Stub!'? Robolectric is a unit test framework that de-fangs the Android SDK jar so you can test drive the development of your App.  Dream of writing tests like this?

<pre>

@RunWith(FastAndroidTestRunner.class)
public class MyActivityTest {
    private MyActivity activity;
    private Button pressMeButton;
    private TextView resultsTextView;
    
    @Before
    public void setUp() throws Exception {
        activity = new MyActivity();
        activity.onCreate(null);
        pressMeButton = (Button) activity.findViewById(R.id.press_me_button_id);
        resultsTextView = (TextView) activity.findViewById(R.id.results_text_view_id);
    }

    @Test
    public void shouldUpdateTheTextOfTheTextViewWhenTheButtonIsPressed() throws Exception {
        pressMeButton.performClick();	
        assertThat(resultsTextView.getText().toString(), equalTo("Testing Android Rocks!"));
    }
}

</pre>

Robolectric makes this possible by intercepting the loading of the Android classes and rewrites the method bodies. By default the methods defined by the SDK jar return null (or 0, false, etc.) instead of throwing a RuntimeException. The other and more important part of what Robolectric does is to proxy to fake Android objects. Robolectric provides a large number of fake objects covering most of what an application would need to test drive the business logic and functionality of your application. Coverage of the SDK is improving every day.

#### View Support

Robolectric handles inflation of views, string resource lookups, etc. Some view attributes (id, visibility,
enabled, text, checked, and src) are currently parsed and applied to inflated views. Activities and Views <code>#findViewById()</code> methods to return objects representing the view, with support for include and merge tags. This allows tests to assert on view state such as visibility and enabled state.

#### Run Tests Outside of the Emulator

Run your tests on your workstation, or on your Continuous Integration environment. Because tests run on your workstation (and not in the emulator), the code generation, dexing, and packaging steps are not necessary, allowing you to iterate quickly and refactor your code with confidence.

## Contributing

We welcome contributions. Please fork and submit pull requests!

## Support

__Group email:__ [robolectric@googlegroups.com](mailto:robolectric@googlegroups.com)  
__Samples:__ [http://github.com/pivotal/RobolectricSample](http://github.com/pivota/RobolectricSample)
