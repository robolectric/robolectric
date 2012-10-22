package com.xtremelabs.robolectric.shadows;

import android.widget.Scroller;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ScrollerTest {
    private Scroller scroller;

    @Before
    public void setup() throws Exception {
        scroller = new Scroller(null, null);
    }

    @Test
    public void shouldScrollOverTime() throws Exception {
        scroller.startScroll(0, 0, 12, 36, 1000);

        assertThat(scroller.getFinalX(), equalTo(12));
        assertThat(scroller.getCurrX(), equalTo(0));
        assertThat(scroller.getCurrY(), equalTo(0));

        Robolectric.idleMainLooper(334);
        assertThat(scroller.getCurrX(), equalTo(4));
        assertThat(scroller.getCurrY(), equalTo(12));

        Robolectric.idleMainLooper(166);
        assertThat(scroller.getCurrX(), equalTo(6));
        assertThat(scroller.getCurrY(), equalTo(18));

        Robolectric.idleMainLooper(500);
        assertThat(scroller.getCurrX(), equalTo(12));
        assertThat(scroller.getCurrY(), equalTo(36));
    }

    @Test
    public void computeScrollOffsetShouldCalculateWhetherScrollIsFinished() throws Exception {

        assertThat(scroller.computeScrollOffset(), equalTo(false));

        scroller.startScroll(0, 0, 12, 36, 1000);
        assertThat(scroller.computeScrollOffset(), equalTo(true));

        Robolectric.idleMainLooper(500);
        assertThat(scroller.computeScrollOffset(), equalTo(true));

        Robolectric.idleMainLooper(500);
        assertThat(scroller.computeScrollOffset(), equalTo(true));
        assertThat(scroller.computeScrollOffset(), equalTo(false));
    }
}
