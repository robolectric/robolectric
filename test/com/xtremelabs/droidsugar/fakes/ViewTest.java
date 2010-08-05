package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ViewTest {
    @Test
    public void testHasEmptyLayoutParams() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);

        ViewGroup.LayoutParams layoutParams = new View(null).getLayoutParams();
        assertThat(layoutParams, notNullValue());
    }
}
