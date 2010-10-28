package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(DogfoodRobolectricTestRunner.class)
public class ViewGroupTest {
    private ViewGroup root;
    private View child1;
    private View child2;
    private ViewGroup child3;
    private View child3a;
    private View child3b;

    @Before public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        root = new FrameLayout(null);

        child1 = new View(null);
        child2 = new View(null);
        child3 = new FrameLayout(null);
        child3a = new View(null);
        child3b = new View(null);

        root.addView(child1);
        root.addView(child2);
        root.addView(child3);

        child3.addView(child3a);
        child3.addView(child3b);
    }

    @Test
    public void testRemoveChildAt() throws Exception {
        root.removeViewAt(1);

        assertThat(root.getChildCount(), equalTo(2));
        assertThat(root.getChildAt(0), sameInstance(child1));
        assertThat(root.getChildAt(1), sameInstance((View) child3));

        assertThat(child2.getParent(), nullValue());
    }

    @Test
    public void hasFocus_shouldReturnTrueIfAnyChildHasFocus() throws Exception {
        assertFalse(root.hasFocus());

        child1.requestFocus();
        assertTrue(root.hasFocus());

        child1.clearFocus();
        assertFalse(root.hasFocus());

        child3b.requestFocus();
        assertTrue(root.hasFocus());

        child3b.clearFocus();
        assertFalse(root.hasFocus());

        root.requestFocus();
        assertTrue(root.hasFocus());
    }

    @Test
    public void clearFocus_shouldRecursivelyClearTheFocusOfAllChildren() throws Exception {
        child3a.requestFocus();

        root.clearFocus();

        assertFalse(child3a.hasFocus());
        assertFalse(child3.hasFocus());
        assertFalse(root.hasFocus());

        root.requestFocus();
        root.clearFocus();
        assertFalse(root.hasFocus());
    }
}
