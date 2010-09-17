package com.xtremelabs.droidsugar.matchers;

import android.view.View;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.fakes.FakeView;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class ViewHasTextMatcher<T extends View> extends TypeSafeMatcher<T> {
    private String expected;
    private String actualText;

    public ViewHasTextMatcher(String expected) {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(View actual) {
        if (actual == null) {
            return false;
        }
        final CharSequence charSequence = proxyFor(actual).innerText();
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

    private FakeView proxyFor(View actual) {
        return ((FakeView) ProxyDelegatingHandler.getInstance().proxyFor(actual));
    }
}
