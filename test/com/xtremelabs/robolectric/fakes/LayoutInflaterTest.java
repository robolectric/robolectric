package com.xtremelabs.robolectric.fakes;

import android.app.Application;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricAndroidTestRunner.class)
public class LayoutInflaterTest {
    private LayoutInflater layoutInflater;

    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addGenericProxies();
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
