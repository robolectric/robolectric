package com.xtremelabs.robolectric.matchers;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import com.xtremelabs.robolectric.shadows.ShadowIntent;
import org.hamcrest.Description;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.getShadowApplication;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class StartedServiceMatcher extends TypeSafeMatcher<Context> {
    private final Intent expectedIntent;

    private String message;

    public StartedServiceMatcher(Intent expectedIntent) {
        this.expectedIntent = expectedIntent;
    }

    public StartedServiceMatcher(String packageName, Class<? extends Service> expectedServiceClass) {
        this(createIntent(packageName, expectedServiceClass));
    }

    public StartedServiceMatcher(Class<? extends Service> expectedServiceClass) {
        this(createIntent(expectedServiceClass));
    }

    public StartedServiceMatcher(Class<? extends Service> expectedServiceClass, String expectedAction) {
        this(createIntent(expectedServiceClass));

        expectedIntent.setAction(expectedAction);
    }

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
        } else {
            // Test that both intent extras have the same keys
            Set<String> keys = shadowIntent.getExtras().keySet();
            Set<String> expectedKeys = shadowOf(expectedIntent).getExtras().keySet();
            intentsMatch = keys.equals(expectedKeys);
            if(!intentsMatch){
                message += "did not get the same extras keys";
            }
        }
        
        return intentsMatch;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(message);
    }

    public static Intent createIntent(Class<? extends Service> serviceClass, String extraKey, String extraValue) {
        Intent intent = createIntent(serviceClass);
        intent.putExtra(extraKey, extraValue);
        return intent;
    }

    public static Intent createIntent(Class<? extends Service> serviceClass, String action) {
        Intent intent = createIntent(serviceClass);
        intent.setAction(action);
        return intent;
    }

    public static Intent createIntent(Class<? extends Service> serviceClass) {
        Package pack = serviceClass.getPackage();
        String packageName = "android.service";
        // getPackage is returning null when run from tests
        if(pack != null) {
            pack.getName();
        }
        return createIntent(packageName, serviceClass);
    }

    public static Intent createIntent(String packageName, Class<? extends Service> serviceClass) {
        Intent intent = new Intent();
        intent.setClassName(packageName, serviceClass.getName());
        intent.setClass(getShadowApplication().getApplicationContext(), serviceClass);
        return intent;
    }
}
