package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.widget.AdapterView;
import android.widget.Gallery;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DroidSugarAndroidTestRunner.class)
public class AbsSpinnerTest {
    private AdapterView adapterView;

    @Before
    public void setUp() throws Exception {
        TestUtil.addAllProxies();

        adapterView = new Gallery(new Activity());
    }

    @Test
    public void shouldHaveAdapterViewCommonBehavior() throws Exception {
        AdapterViewTest.shouldActAsAdapterView(adapterView);
    }
}
