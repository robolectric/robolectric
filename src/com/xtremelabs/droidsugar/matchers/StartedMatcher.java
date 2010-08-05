package com.xtremelabs.droidsugar.matchers;

import android.app.Activity;
import android.content.Intent;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.fakes.FakeActivity;
import com.xtremelabs.droidsugar.fakes.FakeIntent;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class StartedMatcher extends BaseMatcher<Activity> {
    private final boolean matchIntent;
    private final Intent expectedIntent;
    private final Class<? extends Activity> expectedActivityClass;
    private final String expectedAction;

    private Intent actualStartedIntent;
    private Class<?> actualStartedActivityClass;
    private String actualAction;

    public StartedMatcher(Intent expectedIntent) {
        matchIntent = true;
        this.expectedIntent = expectedIntent;
        this.expectedActivityClass = null;
        this.expectedAction = null;
    }

    public StartedMatcher(Class<? extends Activity> expectedActivityClass) {
        this(expectedActivityClass, null);
    }

    public StartedMatcher(Class<? extends Activity> expectedActivityClass, String expectedAction) {
        matchIntent = false;
        this.expectedIntent = null;
        this.expectedActivityClass = expectedActivityClass;
        this.expectedAction = expectedAction;
    }

    @Override
    public boolean matches(Object o) {
        Intent startedActivityIntent = proxyFor((Activity) o).startActivityIntent;

        if (matchIntent) {
            actualStartedIntent = startedActivityIntent;

            return startedActivityIntent != null
                    && proxyFor(expectedIntent).realIntentEquals(proxyFor(startedActivityIntent));
        } else {
            actualStartedActivityClass = proxyFor(startedActivityIntent).componentClass;
            actualAction = startedActivityIntent.getAction();
            return expectedActivityClass.equals(actualStartedActivityClass)
                    && compareStrings(expectedAction, actualAction);
        }
    }

    @Override
    public void describeTo(Description description) {
        if (matchIntent) {
            description.appendText("expected to start " + expectedIntent);
            if (actualStartedIntent == null) {
                description.appendText(" but didn't start anything");
            } else {
                description.appendText(" but started ");
                description.appendValue(actualStartedIntent);
            }
        } else {
            description.appendText("expected to start " + expectedActivityClass);
            if (expectedAction != null) {
                description.appendText(" with action " + expectedAction);
            }
            if (actualStartedActivityClass == null) {
                description.appendText(" but didn't start anything");
            } else {
                description.appendText(" but started " + actualStartedActivityClass);
                if (!compareStrings(expectedAction, actualAction)) {
                    description.appendText(" with " + (actualAction == null ? "no action" : actualAction));
                }
                description.appendText(" instead");
            }
        }
    }

    private FakeActivity proxyFor(Activity real) {
        return (FakeActivity) ProxyDelegatingHandler.getInstance().proxyFor(real);
    }

    private FakeIntent proxyFor(Intent real) {
        return (FakeIntent) ProxyDelegatingHandler.getInstance().proxyFor(real);
    }

    private boolean compareStrings(String a, String b) {
        return a == b || a.equals(b);
    }
}
