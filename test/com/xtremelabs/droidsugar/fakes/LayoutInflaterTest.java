package com.xtremelabs.droidsugar.fakes;

import android.app.Application;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.R;
import com.xtremelabs.droidsugar.res.ResourceLoader;
import com.xtremelabs.droidsugar.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(DroidSugarAndroidTestRunner.class)
public class LayoutInflaterTest {
    private LayoutInflater layoutInflater;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();
        FakeHelper.application = new Application();
        FakeHelper.resourceLoader = new ResourceLoader(R.class, new File("test/res"));
        layoutInflater = LayoutInflater.from(FakeHelper.application);
    }
    
    @Test
    public void getInstance_shouldReturnSameInstance() throws Exception {
        assertNotNull(layoutInflater);
        assertSame(LayoutInflater.from(FakeHelper.application), layoutInflater);
        assertSame(LayoutInflater.from(new ContextWrapper(FakeHelper.application)), layoutInflater);
    }
}
