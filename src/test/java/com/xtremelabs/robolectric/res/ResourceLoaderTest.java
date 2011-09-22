package com.xtremelabs.robolectric.res;


import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.I18nException;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ResourceLoaderTest {
    @Test
    public void shouldUseFileSystemSeparatorWhenEvaluatingLayoutDirectories() throws Exception {
        assertTrue(ResourceLoader.isLayoutDirectory(File.separator + "layout"));
    }

    @Test
    public void shouldLoadSystemResources() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(10, R.class, resourceFile("res"), resourceFile("assets"));
        String stringValue = resourceLoader.getStringValue(android.R.string.copy);
        assertEquals("Copy", stringValue);

        ViewLoader.ViewNode node = resourceLoader.getLayoutViewNode("android:layout/simple_spinner_item");
        assertNotNull(node);
    }

    @Test
    public void shouldLoadLocalResources() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(10, R.class, resourceFile("res"), resourceFile("assets"));
        String stringValue = resourceLoader.getStringValue(R.string.copy);
        assertEquals("Local Copy", stringValue);
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateView() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(10, R.class, resourceFile("res"), resourceFile("layout"));
        resourceLoader.setStrictI18n(true);
        ViewGroup vg = new FrameLayout(Robolectric.application);
    	resourceLoader.inflateView(Robolectric.application, R.layout.text_views, vg);
    }
    
    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateMenu() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(10, R.class, resourceFile("res"), resourceFile("menu"));
        resourceLoader.setStrictI18n(true);
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test, null);
    }
    
    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflatePreferences() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(10, R.class, resourceFile("res"), resourceFile("xml"));
        resourceLoader.setStrictI18n(true);
    	resourceLoader.inflatePreferences(Robolectric.application, R.xml.preferences);
    }

}
