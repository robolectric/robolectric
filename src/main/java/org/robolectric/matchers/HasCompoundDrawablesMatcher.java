package com.xtremelabs.robolectric.matchers;

import android.widget.TextView;
import com.xtremelabs.robolectric.shadows.ShadowTextView;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class HasCompoundDrawablesMatcher extends TypeSafeMatcher<TextView> {
    private String message;
    private ShadowTextView.CompoundDrawables expectedCompoundDrawables;

    public HasCompoundDrawablesMatcher(int left, int top, int right, int bottom) {
        expectedCompoundDrawables = new ShadowTextView.CompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean matchesSafely(TextView actual) {
        if (actual == null) {
            message = "actual view was null";
            return false;
        }

        ShadowTextView.CompoundDrawables actualCompoundDrawables = shadowOf(actual).getCompoundDrawablesImpl();
        if (!expectedCompoundDrawables.equals(actualCompoundDrawables)) {
            message = "[" + actualCompoundDrawables + "] to equal [" + expectedCompoundDrawables + "]";
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(message);
    }

    @Factory
    public static Matcher<TextView> hasCompoundDrawables(int left, int top, int right, int bottom) {
        return new HasCompoundDrawablesMatcher(left, top, right, bottom);
    }
}
