package com.xtremelabs.robolectric.matchers;

import android.widget.ImageView;
import com.xtremelabs.robolectric.res.ResourceLoader;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

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

        ResourceLoader resourceLoader = ResourceLoader.getFrom(actualImageView.getContext());

        int actualResourceId = shadowOf(actualImageView).getResourceId();
        String actualName = nameOrUnset(resourceLoader, actualResourceId);
        String expectedName = nameOrUnset(resourceLoader, expectedResourceId);
        message = "[" + actualResourceId + " (" + actualName + ")] to equal [" + expectedResourceId + " (" + expectedName + ")]";
        return actualResourceId == expectedResourceId;
    }

    private String nameOrUnset(ResourceLoader resourceLoader, int resourceId) {
        return resourceId == 0 ? "unset" : resourceLoader.getNameForId(resourceId);
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
