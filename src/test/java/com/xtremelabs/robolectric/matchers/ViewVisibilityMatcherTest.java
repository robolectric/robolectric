package com.xtremelabs.robolectric.matchers;

import android.view.View;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ViewVisibilityMatcherTest {

    private View visibleView;
    private View invisibleView;
    private View goneView;

    @Before public void setUp() throws Exception {
        visibleView = new View(null);
        visibleView.setVisibility(View.VISIBLE);
        invisibleView = new View(null);
        invisibleView.setVisibility(View.INVISIBLE);
        goneView = new View(null);
        goneView.setVisibility(View.GONE);
    }

    @Test
    public void visibleMatch() throws Exception {
        Assert.assertThat(visibleView, ViewVisibilityMatcher.isVisible());
        Assert.assertThat(invisibleView, not(ViewVisibilityMatcher.isVisible()));
        Assert.assertThat(goneView, not(ViewVisibilityMatcher.isVisible()));
    }

    @Test
    public void invisibleMatch() throws Exception {
        Assert.assertThat(visibleView, not(ViewVisibilityMatcher.isInvisible()));
        Assert.assertThat(invisibleView, ViewVisibilityMatcher.isInvisible());
        Assert.assertThat(goneView, not(ViewVisibilityMatcher.isInvisible()));
    }

    @Test
    public void goneMatch() throws Exception {
        Assert.assertThat(visibleView, not(ViewVisibilityMatcher.isGone()));
        Assert.assertThat(invisibleView, not(ViewVisibilityMatcher.isGone()));
        Assert.assertThat(goneView, ViewVisibilityMatcher.isGone());
    }

    @Test
    public void descriptionShouldIndicateExpectedAndActual() throws Exception {
        Matcher<View> goneMatcher = ViewVisibilityMatcher.isGone();
        goneMatcher.matches(visibleView);
        StringDescription description = new StringDescription();
        goneMatcher.describeTo(description);
        assertEquals("[0] visibility to be [8]", description.toString());
    }
}
