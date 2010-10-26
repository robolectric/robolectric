package com.xtremelabs.robolectric.matchers;

import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ViewHasTextMatcher<T extends View> extends TypeSafeMatcher<T> {
    private String expected;
    private int expectedResourceId;
    private String actualText;

    public ViewHasTextMatcher(String expected) {
        this.expected = expected;
        expectedResourceId = -1;
    }

    public ViewHasTextMatcher(int expectedResourceId) {
        this.expected = null;
        this.expectedResourceId = expectedResourceId;
    }

    @Override
    public boolean matchesSafely(View actual) {
        if (actual == null) {
            return false;
        }

        if (expectedResourceId != -1) {
            expected = actual.getContext().getResources().getString(expectedResourceId);
        }

        final CharSequence charSequence = shadowOf(actual).innerText();
        if (charSequence == null || charSequence.toString() == null) {
            return false;
        }
        actualText = charSequence.toString();

        return actualText.equals(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[" + actualText + "]");
        description.appendText(" to equal ");
        description.appendText("[" + expected + "]");
    }


    @Factory
    public static <T extends View> Matcher<T> hasText(String expectedTextViewText) {
        return new ViewHasTextMatcher<T>(expectedTextViewText);
    }

    @Factory
    public static <T extends View> Matcher<T> hasText(int expectedTextViewResourceId) {
        return new ViewHasTextMatcher<T>(expectedTextViewResourceId);
    }
}
