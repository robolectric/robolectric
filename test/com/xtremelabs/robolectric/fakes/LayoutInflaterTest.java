package com.xtremelabs.robolectric.fakes;

import android.app.Application;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.res.ResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(DogfoodRobolectricTestRunner.class)
public class LayoutInflaterTest {
    private LayoutInflater layoutInflater;

    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();
        Robolectric.application = FakeApplication.bind(new Application(), new ResourceLoader(R.class, new File("test/res")));
        layoutInflater = LayoutInflater.from(Robolectric.application);
    }
    
    @Test
    public void getInstance_shouldReturnSameInstance() throws Exception {
        assertNotNull(layoutInflater);
        assertSame(LayoutInflater.from(Robolectric.application), layoutInflater);
        assertSame(LayoutInflater.from(new ContextWrapper(Robolectric.application)), layoutInflater);
    }
}
