package com.xtremelabs.robolectric.matchers;

import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.HashMap;
import java.util.Map;

public class ViewVisibilityMatcher<T extends View> extends TypeSafeMatcher<T> {

    private static final Map<Integer, String> VISIBILITY_DESCRIPTIONS;
    static {
        VISIBILITY_DESCRIPTIONS = new HashMap<Integer, String>();
        VISIBILITY_DESCRIPTIONS.put(View.VISIBLE, "'Visible'");
        VISIBILITY_DESCRIPTIONS.put(View.INVISIBLE, "'Invisible'");
        VISIBILITY_DESCRIPTIONS.put(View.GONE, "'Gone'");
    }

    private final int expectedVisibility;
    private int actualVisibility = -1;

    public ViewVisibilityMatcher(int expectedVisibility) {
        this.expectedVisibility = expectedVisibility;
    }

    @Override
    public boolean matchesSafely(T t) {
        if (t == null){
            return false;
        }
        actualVisibility = t.getVisibility();
        return expectedVisibility == actualVisibility;
    }

    @Override
    public void describeTo(Description description) {
        if (actualVisibility >= 0){
            description.appendText(VISIBILITY_DESCRIPTIONS.get(actualVisibility));
            description.appendText(" to be ");
            description.appendText(VISIBILITY_DESCRIPTIONS.get(expectedVisibility));
        } else {
            description.appendText("View to be non-null.");
        }
    }

    @Factory
    public static <T extends View> Matcher<T> isVisible() {
        return new ViewVisibilityMatcher<T>(View.VISIBLE);
    }

    @Factory
    public static <T extends View> Matcher<T> isInvisible() {
        return new ViewVisibilityMatcher<T>(View.INVISIBLE);
    }

    @Factory
    public static <T extends View> Matcher<T> isGone() {
        return new ViewVisibilityMatcher<T>(View.GONE);
    }
}
