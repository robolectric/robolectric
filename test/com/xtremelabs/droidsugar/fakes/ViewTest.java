package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ViewTest {

    private View view;

    @Before public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();

        view = new View(null);
    }

    @Test
    public void testHasEmptyLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        assertThat(layoutParams, notNullValue());
    }

    @Test
    public void layout_shouldAffectWidthAndHeight() throws Exception {
        assertThat(view.getWidth(), equalTo(0));
        assertThat(view.getHeight(), equalTo(0));

        view.layout(100, 200, 303, 404);
        assertThat(view.getWidth(), equalTo(303 - 100));
        assertThat(view.getHeight(), equalTo(404 - 200));
    }
}
