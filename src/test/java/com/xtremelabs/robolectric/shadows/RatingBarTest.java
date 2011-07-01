package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.widget.RatingBar;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class RatingBarTest {

    @Test
    public void testInheritance() {
        TestRatingBar ratingBar = new TestRatingBar(new Activity());
        ShadowRatingBar shadow = Robolectric.shadowOf(ratingBar);
        assertThat(shadow, instanceOf(ShadowAbsSeekBar.class));
    }
    
    private static class TestRatingBar extends RatingBar {
        
        public TestRatingBar(Context context) {
            super(context);
        }
    }
}
