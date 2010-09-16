package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ViewGroupTest {
    public ViewGroup viewGroup;

    @Before public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();
        
        viewGroup = new FrameLayout(null);
    }

    @Test
    public void testRemoveChildAt() throws Exception {
        View child1 = new View(null);
        View child2 = new View(null);
        View child3 = new View(null);
        viewGroup.addView(child1);
        viewGroup.addView(child2);
        viewGroup.addView(child3);

        viewGroup.removeViewAt(1);

        assertThat(viewGroup.getChildCount(), equalTo(2));
        assertThat(viewGroup.getChildAt(0), sameInstance(child1));
        assertThat(viewGroup.getChildAt(1), sameInstance(child3));

        assertThat(child2.getParent(), nullValue());
    }
}
