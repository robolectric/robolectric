---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

# Making TDD for your Android Application possible

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

Robolectric makes this possible by intercepting the loading of the Android classes and rewrites the method bodies. By default the methods defined by the SDK jar return null (or 0, false, etc.) instead of throwing a RuntimeException. The other, and more important, part of what Roblectric does is to delegate to fake Android objects. Robolectric provides a large number of fake objects covering most of what an application would need to test drive the business logic and functionality. Layout and other XML resources are parsed and loaded. Headless versions of view classes are loaded allowing #findViewById() methods to return objects representing the view. This allows tests to assert on view state such as visibility and enabled state.

Because you are writing your tests with JUnit, you can run your tests on your workstation, or on your Continuous Integration environment.

## Support

__Group email:__ [robolectric@googlegroups.com](mailto:robolectric@googlegroups.com)  
