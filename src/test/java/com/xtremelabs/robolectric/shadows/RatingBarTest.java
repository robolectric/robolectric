package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.widget.RatingBar;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;

@RunWith(WithTestDefaultsRunner.class)
public class RatingBarTest {

    private RatingBar ratingBar;
    private ShadowRatingBar shadow;
    private RatingBar.OnRatingBarChangeListener listener;
    private Transcript transcript;
    
    @Before
    public void setup() {
        ratingBar = new RatingBar(new Activity());
        shadow = Robolectric.shadowOf(ratingBar);
        listener = new TestRatingBarChangedListener();
        transcript = new Transcript();
        ratingBar.setOnRatingBarChangeListener(listener); 
    }
    
    @Test
    public void testOnSeekBarChangedListener() {
        assertThat(shadow.getOnRatingBarChangeListener(), sameInstance(listener));
        ratingBar.setOnRatingBarChangeListener(null);
        assertThat(shadow.getOnRatingBarChangeListener(), nullValue());
    }
    
    @Test
    public void testOnChangeNotification() {
        ratingBar.setRating(5.0f);
        transcript.assertEventsSoFar("onRatingChanged() - 5.0");
    }
    
    private class TestRatingBarChangedListener implements RatingBar.OnRatingBarChangeListener {

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            transcript.add("onRatingChanged() - " + rating);
        }
    }
    
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
