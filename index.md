---
layout: default
title: "Robolectric: Unit Test your Android Application"
---

# Robolectric

## Test-Drive Your Android Code

Running tests on an Android emulator or device is slow! Building, deploying, and launching the app often takes a minute
or more. That's no way to do TDD. There must be a better way.

Wouldn't it be nice to run your Android tests directly from inside your IDE? Perhaps you've tried, and been thwarted by
the dreaded `java.lang.RuntimeException: Stub!`?

[Robolectric](http://robolectric.org/) is a unit test framework that de-fangs the Android SDK jar so you
can test-drive the development of your Android app.  Tests run inside the JVM on your workstation in seconds. With
Robolectric you can write tests like this:

```java
// Test class for MyActivity
@RunWith(RobolectricTestRunner.class)
public class MyActivityTest {

  @Test
  public void clickingButton_shouldChangeResultsViewText() throws Exception {
    Activity activity = Robolectric.buildActivity(MyActivity.class).create().get();

    Button pressMeButton = (Button) activity.findViewById(R.id.press_me_button);
    TextView results = (TextView) activity.findViewById(R.id.results_text_view);

    pressMeButton.performClick();
    String resultsText = results.getText().toString();
    assertThat(resultsText, equalTo("Testing Android Rocks!"));
  }
}
```

Robolectric makes this possible by rewriting Android SDK classes as they're being loaded and making it possible for them
to run on a regular JVM.

### SDK, Resources, & Native Method Emulation

Robolectric handles inflation of views, resource loading, and lots of other stuff that's implemented in native C code on
Android devices. This allows tests to do most things you could do on a real device. It's easy to provide our own
implementation for specific SDK methods too, so you could simulate error conditions or real-world sensor behavior, for
example.

### Run Tests Outside of the Emulator

Robolectric lets you run your tests on your workstation, or on your Continuous Integration environment in a regular JVM,
without an emulator. Because of this, the dexing, packaging, and installing-on-the emulator steps aren't necessary,
reducing test cycles from minutes to seconds so you can iterate quickly and refactor your code with confidence.

### No Mocking Frameworks Required

An alternate approach to Robolectric is to use mock frameworks such as [Mockito](http://code.google.com/p/mockito/) or
[Android Mock](http://code.google.com/p/android-mock/) to mock out the Android SDK. While this is a valid approach, it
often yields tests that are essentially reverse implementations of the application code.

Robolectric allows a test style that is closer to black box testing, making the tests more effective for refactoring and
allowing the tests to focus on the behavior of the application instead of the implementation of Android. You can still
use a mocking framework along with Robolectric if you like.

## Contributing

We welcome contributions. Please [fork](http://github.com/robolectric/robolectric) and submit pull requests. Don't forget to include tests!

## Sample Project

Look at the [sample project](https://github.com/robolectric/robolectricsample) to see how fast and easy it can be to test
drive the development of Android applications.

## Robolectric's current maintainers:

* [Aaron VonderHaar](https://github.com/avh4), Pivotal Labs
* [Christian Williams](http://github.com/Xian), Square
* [Jan Berkel](https://github.com/jberkel), SoundCloud
* [Jake Wharton](https://github.com/JakeWharton), Square
* [Michael Portuesi](https://github.com/mportuesisf), Zoodles
* [Phil Goodwin](https://github.com/pgoodwin), Pivotal Labs
* [Rick Kawala](https://github.com/rkawala), Pivotal Labs
* [Tyler Schultz](https://github.com/tylerschultz), Pivotal Labs

## Acknowledgments

* Robolectric was originally developed by [Christian Williams](http://github.com/Xian) at [XtremeLabs](http://www.xtremelabs.com/). Big thanks to XtremeLabs for their support.
* Considerable contributions have been made by the teams at Pivotal Labs, Zoodles, Square, and Soundcloud. Thanks to those companies for their support!
* Thanks to [Shane Francis](http://shanefrancis.com/) for the (Philip K. Dick-)inspired name, and to Pivots [Ofri Afek](mailto:ofri@pivotallabs.com) and [Jessica Miller](mailto:jessica@pivotallabs.com) for our logo!

## Support

__Google Group:__ [http://groups.google.com/group/robolectric](http://groups.google.com/group/robolectric)<br/>
__Group Email:__ [robolectric@googlegroups.com](mailto:robolectric@googlegroups.com)<br/>
__Samples:__ [http://github.com/robolectric/RobolectricSample](http://github.com/robolectric/RobolectricSample)

