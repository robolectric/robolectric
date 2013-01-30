package org.robolectric.shadows;

import android.widget.ScrollView;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class ScrollViewTest {
    @Test
    public void shouldSmoothScrollTo() throws Exception {
        ScrollView scrollView = new ScrollView(null);
        scrollView.smoothScrollTo(7, 6);

        assertEquals(7, scrollView.getScrollX());
        assertEquals(6, scrollView.getScrollY());
    }
}
