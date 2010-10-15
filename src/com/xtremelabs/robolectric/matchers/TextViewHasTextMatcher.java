package com.xtremelabs.robolectric.matchers;

import android.widget.TextView;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class TextViewHasTextMatcher<T extends TextView> extends TypeSafeMatcher<T> {
    private String expected;
    private String actualText;

    public TextViewHasTextMatcher(String expected) {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(T actual) {
        if (actual == null) {
            return false;
        }
        final CharSequence charSequence = actual.getText();
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
    public static <T extends TextView> Matcher<T> hasText(String expectedTextViewText) {
        return new TextViewHasTextMatcher<T>(expectedTextViewText);
    }
}
