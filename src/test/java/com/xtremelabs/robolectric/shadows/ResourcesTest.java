package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.File;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

@RunWith(WithTestDefaultsRunner.class)
public class ResourcesTest {
	
	@Before
	public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();		
        Robolectric.application = ShadowApplication.bind(new Application(), new ResourceLoader(R.class, new File("test/res")));
	}
	
    @Test(expected = Resources.NotFoundException.class)
    public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        Robolectric.application = ShadowApplication.bind(new Application(), new ResourceLoader(R.class, resourceFile("res"), resourceFile("assets")));

        new Activity().getResources().getStringArray(-1);
    }
    
    @Test
    public void testConfiguration() {
        assertThat( new Activity().getResources().getConfiguration(), notNullValue() );
    }
}
