package com.xtremelabs.robolectric.matchers;

import android.view.View;
import com.xtremelabs.robolectric.TestRunners;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.matchers.ViewVisibilityMatcher.*;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
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
        assertThat(visibleView, isVisible());
        assertThat(invisibleView, not(isVisible()));
        assertThat(goneView, not(isVisible()));
        assertThat(null, not(isVisible()));
    }

    @Test
    public void invisibleMatch() throws Exception {
        assertThat(visibleView, not(isInvisible()));
        assertThat(invisibleView, isInvisible());
        assertThat(goneView, not(isInvisible()));
        assertThat(null, not(isInvisible()));
    }

    @Test
    public void goneMatch() throws Exception {
        assertThat(visibleView, not(isGone()));
        assertThat(invisibleView, not(isGone()));
        assertThat(goneView, isGone());
        assertThat(null, not(isGone()));
    }

    @Test
    public void descriptionShouldIndicateExpectedAndActual() {
        Matcher<View> goneMatcher = isGone();
        goneMatcher.matches(visibleView);
        StringDescription description = new StringDescription();
        goneMatcher.describeTo(description);
        assertEquals("'Visible' to be 'Gone'", description.toString());
    }

    @Test
    public void descriptionShouldIndicateNullView() {
        Matcher<View> goneMatcher = isGone();
        goneMatcher.matches(null);
        StringDescription description = new StringDescription();
        goneMatcher.describeTo(description);
        assertEquals("View to be non-null.", description.toString());
    }
}
