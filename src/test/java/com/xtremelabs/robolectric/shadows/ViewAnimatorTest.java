package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.view.View;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ViewAnimatorTest {
    protected ViewAnimator animator;

    @Before
    public void setUp() {
        animator = new ViewAnimator(new Activity());
    }

    @Test
    public void testHappyPath() {
        View v = new View(null);
        animator.addView(v);
        
        assertEquals(0, animator.getDisplayedChild());
        assertEquals(v, animator.getCurrentView());
    }

    @Test
    public void testAnimatorHandlesCyclingViews() {
        View v1 = new View(null);
        View v2 = new View(null);
        
        animator.addView(v1);
        animator.addView(v2);
        
        animator.showNext();
        
        assertEquals(1, animator.getDisplayedChild());
        assertEquals(v2, animator.getCurrentView());
    }
}
