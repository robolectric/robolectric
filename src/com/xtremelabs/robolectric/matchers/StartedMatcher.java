package com.xtremelabs.robolectric.matchers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import com.xtremelabs.robolectric.shadows.ShadowIntent;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class StartedMatcher extends TypeSafeMatcher<Context> {
    private final Intent expectedIntent;

    private String message;

    public StartedMatcher(Intent expectedIntent) {
        this.expectedIntent = expectedIntent;
    }

    public StartedMatcher(String packageName, Class<? extends Activity> expectedActivityClass) {
        this(createIntent(packageName, expectedActivityClass));
    }

    public StartedMatcher(Class<? extends Activity> expectedActivityClass) {
        this(createIntent(expectedActivityClass));
    }

    public StartedMatcher(Class<? extends Activity> expectedActivityClass, String expectedAction) {
        this(createIntent(expectedActivityClass));

        expectedIntent.setAction(expectedAction);
    }

    @Override
    public boolean matchesSafely(Context actualContext) {
        if (expectedIntent == null) {
            message = "null intent (did you mean to expect null?)";
            return false;
        }

        String expected = expectedIntent.toString();
        message = "to start " + expected + ", but ";

        Intent actualStartedIntent = shadowOf((ContextWrapper) actualContext).getNextStartedActivity();

        if (actualStartedIntent == null) {
            message += "didn't start anything";
            return false;
        }

        ShadowIntent shadowIntent = shadowOf(actualStartedIntent);

        boolean intentsMatch = shadowOf(expectedIntent).realIntentEquals(shadowIntent);
        if (!intentsMatch) {
            message += "started " + actualStartedIntent;
        }
        return intentsMatch;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(message);
    }

    public static Intent createIntent(Class<? extends Activity> activityClass, String extraKey, String extraValue) {
        Intent intent = createIntent(activityClass);
        intent.putExtra(extraKey, extraValue);
        return intent;
    }

    public static Intent createIntent(Class<? extends Activity> activityClass, String action) {
        Intent intent = createIntent(activityClass);
        intent.setAction(action);
        return intent;
    }

    public static Intent createIntent(Class<? extends Activity> activityClass) {
        String packageName = activityClass.getPackage().getName();
        return createIntent(packageName, activityClass);
    }

    public static Intent createIntent(String packageName, Class<? extends Activity> activityClass) {
        Intent intent = new Intent();
        intent.setClassName(packageName, activityClass.getName());
        return intent;
    }
}
