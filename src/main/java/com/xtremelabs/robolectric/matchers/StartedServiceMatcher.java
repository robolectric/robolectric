package com.xtremelabs.robolectric.matchers;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import com.xtremelabs.robolectric.shadows.ShadowIntent;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class StartedServiceMatcher extends TypeSafeMatcher<Context> {
    private final Intent expectedIntent;

    private String message;

    public StartedServiceMatcher(Intent expectedIntent) {
        this.expectedIntent = expectedIntent;
    }

//    public StartedMatcher(String packageName, Class<? extends Activity> expectedActivityClass) {
//        this(createIntent(packageName, expectedActivityClass));
//    }
//
//    public StartedMatcher(Class<? extends Activity> expectedActivityClass) {
//        this(createIntent(expectedActivityClass));
//    }
//
//    public StartedMatcher(Class<? extends Activity> expectedActivityClass, String expectedAction) {
//        this(createIntent(expectedActivityClass));
//
//        expectedIntent.setAction(expectedAction);
//    }

    /**
     * Check if the class of the intent and the keys of the intent's extras match
     * 
     * @param actualContext
     * @return
     */
    @Override
    public boolean matchesSafely(Context actualContext) {
        if (expectedIntent == null) {
            message = "null intent (did you mean to expect null?)";
            return false;
        }

        String expected = expectedIntent.toString();
        message = "to start " + expected + ", but ";

        Intent actualStartedIntent = shadowOf((ContextWrapper) actualContext).getNextStartedService();

        if (actualStartedIntent == null) {
            message += "didn't start anything";
            return false;
        }

        ShadowIntent shadowIntent = shadowOf(actualStartedIntent);

        //boolean intentsMatch = shadowOf(expectedIntent).realIntentEquals(shadowIntent);
        // Test only that we are sending intent to the right service class
        boolean intentsMatch = shadowOf(expectedIntent).getIntentClass().equals(shadowIntent.getIntentClass());
        if (!intentsMatch) {
            message += "started " + actualStartedIntent;
        }

        Set<String> keys = shadowIntent.getExtras().keySet();
        Set<String> expectedKeys = shadowOf(expectedIntent).getExtras().keySet();
        intentsMatch = keys.equals(expectedKeys);
        if(!intentsMatch){
            message += "did not get the same extras keys";
        }
        return intentsMatch;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(message);
    }

//    public static Intent createIntent(Class<? extends Activity> activityClass, String extraKey, String extraValue) {
//        Intent intent = createIntent(activityClass);
//        intent.putExtra(extraKey, extraValue);
//        return intent;
//    }
//
//    public static Intent createIntent(Class<? extends Activity> activityClass, String action) {
//        Intent intent = createIntent(activityClass);
//        intent.setAction(action);
//        return intent;
//    }
//
//    public static Intent createIntent(Class<? extends Activity> activityClass) {
//        String packageName = activityClass.getPackage().getName();
//        return createIntent(packageName, activityClass);
//    }
//
//    public static Intent createIntent(String packageName, Class<? extends Activity> activityClass) {
//        Intent intent = new Intent();
//        intent.setClassName(packageName, activityClass.getName());
//        return intent;
//    }
}
