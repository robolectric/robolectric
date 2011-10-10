package com.xtremelabs.robolectric.matchers;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.service.wallpaper.WallpaperService;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;
import static com.xtremelabs.robolectric.matchers.StartedServiceMatcher.createIntent;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class StartedServiceMatcherTest {
    private WallpaperService service;
    private Intent intentWithExtra;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        Robolectric.resetStaticState();

        service = new WallpaperService() {
            @Override
            public Engine onCreateEngine() {
                return null;
            }
        };
        intentWithExtra = createIntent(WallpaperService.class, "someExtra", "value");
    }
//
    @Test
    public void shouldSayDidntStartAnythingIfNothingWasStarted() throws Exception {
        assertThat(new StartedServiceMatcher(WallpaperService.class),
                givesFailureMessage((Context) service, "to start " + createIntent(WallpaperService.class) + ", but didn't start anything"));

        assertThat(new StartedServiceMatcher(WallpaperService.class, "view"),
                givesFailureMessage((Context) service, "to start " + createIntent(WallpaperService.class, "view") + ", but didn't start anything"));

        assertThat(new StartedServiceMatcher(intentWithExtra),
                givesFailureMessage((Context) service, "to start " + intentWithExtra + ", but didn't start anything"));
    }

    @Test
    public void shouldSayStartedSomethingIfWrongThingWasStarted() throws Exception {
        Intent actualIntent = createIntent(WallpaperService.class, "anotherExtra", "anotherValue");

        service.startService(actualIntent);
        assertThat(new StartedServiceMatcher(IntentService.class),
                givesFailureMessage((Context) service, "to start " + createIntent(IntentService.class) + ", but started " + actualIntent));

        service.startService(actualIntent);
        assertThat(new StartedServiceMatcher(IntentService.class, "view"),
                givesFailureMessage((Context) service, "to start " + createIntent(IntentService.class, "view") + ", but started " + actualIntent));

        service.startService(actualIntent);
        assertThat(new StartedServiceMatcher(intentWithExtra),
                givesFailureMessage((Context) service, "to start " + intentWithExtra + ", but did not get the same extras keys"));
    }

    private <T> Matcher<Matcher<T>> givesFailureMessage(final T actual, final String expectedFailureMessage) {
        return new TypeSafeMatcher<Matcher<T>>() {
            public String message;

            @Override
            public boolean matchesSafely(Matcher<T> tMatcher) {
                if (tMatcher.matches(actual)) {
                    message = "matcher to fail, but it passed";
                    return false;
                }
                StringDescription description = new StringDescription();
                tMatcher.describeTo(description);
                String actualFailureMessage = description.toString();
                if (expectedFailureMessage.equals(actualFailureMessage)) {
                    return true;
                } else {
                    message = "failure message to be [" + expectedFailureMessage + "] but got [" + actualFailureMessage + "]";
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(message);
            }
        };
    }

}
