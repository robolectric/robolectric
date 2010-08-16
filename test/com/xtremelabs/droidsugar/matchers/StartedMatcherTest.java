package com.xtremelabs.droidsugar.matchers;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AliasActivity;
import android.app.ListActivity;
import android.content.Intent;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.fakes.FakeActivity;
import com.xtremelabs.droidsugar.fakes.FakeIntent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.matchers.StartedMatcher.createIntent;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class StartedMatcherTest {
    private Activity activity;
    private Intent intentWithExtra;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Activity.class, FakeActivity.class);
        DroidSugarAndroidTestRunner.addProxy(Intent.class, FakeIntent.class);

        activity = new Activity();
        intentWithExtra = createIntent(AliasActivity.class, "someExtra", "value");
    }

    @Test
    public void shouldSayDidntStartAnythingIfNothingWasStarted() throws Exception {
        assertThat(new StartedMatcher(ActivityGroup.class),
                givesFailureMessage(activity, "to start " + createIntent(ActivityGroup.class) + ", but didn't start anything"));

        assertThat(new StartedMatcher(ActivityGroup.class, "view"),
                givesFailureMessage(activity, "to start " + createIntent(ActivityGroup.class, "view") + ", but didn't start anything"));

        assertThat(new StartedMatcher(intentWithExtra),
                givesFailureMessage(activity, "to start " + intentWithExtra + ", but didn't start anything"));
    }

    @Test
    public void shouldSayStartedSomethingIfWrongThingWasStarted() throws Exception {
        Intent actualIntent = createIntent(ListActivity.class, "anotherExtra", "anotherValue");
        activity.startActivity(actualIntent);

        assertThat(new StartedMatcher(ActivityGroup.class),
                givesFailureMessage(activity, "to start " + createIntent(ActivityGroup.class) + ", but started " + actualIntent));

        assertThat(new StartedMatcher(ActivityGroup.class, "view"),
                givesFailureMessage(activity, "to start " + createIntent(ActivityGroup.class, "view") + ", but started " + actualIntent));

        assertThat(new StartedMatcher(intentWithExtra),
                givesFailureMessage(activity, "to start " + intentWithExtra + ", but started " + actualIntent));
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
