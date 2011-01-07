package com.xtremelabs.robolectric.matchers;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AliasActivity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.matchers.StartedMatcher.createIntent;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class StartedMatcherTest {
    private Activity activity;
    private Intent intentWithExtra;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        Robolectric.resetStaticState();

        activity = new Activity();
        intentWithExtra = createIntent(AliasActivity.class, "someExtra", "value");
    }

    @Test
    public void shouldSayDidntStartAnythingIfNothingWasStarted() throws Exception {
        assertThat(new StartedMatcher(ActivityGroup.class),
                givesFailureMessage((Context) activity, "to start " + createIntent(ActivityGroup.class) + ", but didn't start anything"));

        assertThat(new StartedMatcher(ActivityGroup.class, "view"),
                givesFailureMessage((Context) activity, "to start " + createIntent(ActivityGroup.class, "view") + ", but didn't start anything"));

        assertThat(new StartedMatcher(intentWithExtra),
                givesFailureMessage((Context) activity, "to start " + intentWithExtra + ", but didn't start anything"));
    }

    @Test
    public void shouldSayStartedSomethingIfWrongThingWasStarted() throws Exception {
        Intent actualIntent = createIntent(ListActivity.class, "anotherExtra", "anotherValue");

        activity.startActivity(actualIntent);
        assertThat(new StartedMatcher(ActivityGroup.class),
                givesFailureMessage((Context) activity, "to start " + createIntent(ActivityGroup.class) + ", but started " + actualIntent));

        activity.startActivity(actualIntent);
        assertThat(new StartedMatcher(ActivityGroup.class, "view"),
                givesFailureMessage((Context) activity, "to start " + createIntent(ActivityGroup.class, "view") + ", but started " + actualIntent));

        activity.startActivity(actualIntent);
        assertThat(new StartedMatcher(intentWithExtra),
                givesFailureMessage((Context) activity, "to start " + intentWithExtra + ", but started " + actualIntent));
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
