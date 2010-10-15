package com.xtremelabs.robolectric.matchers;

import android.widget.ImageView;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.fakes.FakeImageView;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class ImageViewHasDrawableMatcher<T extends ImageView> extends TypeSafeMatcher<T> {
    private int expectedResourceId;
    private String message;

    public ImageViewHasDrawableMatcher(int expectedResourceId) {
        this.expectedResourceId = expectedResourceId;
    }

    @Override
    public boolean matchesSafely(T actualImageView) {
        if (actualImageView == null) {
            return false;
        }

        int actualResourceId = ((FakeImageView) ProxyDelegatingHandler.getInstance().proxyFor(actualImageView)).resourceId;
        message = "[" + actualResourceId + "] to equal [" + expectedResourceId + "]";
        return actualResourceId == expectedResourceId;
    }


    @Override
    public void describeTo(Description description) {
        description.appendText(message);
    }

    @Factory
    public static <T extends ImageView> Matcher<T> hasDrawable(int expectedResourceId) {
        return new ImageViewHasDrawableMatcher<T>(expectedResourceId);
    }
}
