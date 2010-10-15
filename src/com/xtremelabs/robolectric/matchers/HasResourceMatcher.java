package com.xtremelabs.robolectric.matchers;

import android.widget.ImageView;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.fakes.FakeImageView;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class HasResourceMatcher extends TypeSafeMatcher<ImageView> {
    private int expectedResourceId;
    private Integer actualResourceId;

    public HasResourceMatcher(int expectedResourceId) {
        this.expectedResourceId = expectedResourceId;
    }

    @Override
    public boolean matchesSafely(ImageView actual) {
        if (actual == null) {
            return false;
        }

        actualResourceId = proxyFor(actual).resourceId;

        return actualResourceId == expectedResourceId;
    }

    @Override
    public void describeTo(Description description) {
        if (actualResourceId == null) {
            description.appendText("actual view was null");
        } else {
            description.appendText("[" + actualResourceId + "]");
            description.appendText(" to equal ");
            description.appendText("[" + expectedResourceId + "]");
        }
    }

    @Factory
    public static Matcher<ImageView> hasResource(int expectedResourceId) {
        return new HasResourceMatcher(expectedResourceId);
    }

    private FakeImageView proxyFor(ImageView actual) {
        return (FakeImageView) ProxyDelegatingHandler.getInstance().proxyFor(actual);
    }
}