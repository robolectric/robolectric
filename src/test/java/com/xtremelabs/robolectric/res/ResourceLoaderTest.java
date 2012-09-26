package com.xtremelabs.robolectric.res;


import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.I18nException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class ResourceLoaderTest {
    @Test
    public void shouldUseFileSystemSeparatorWhenEvaluatingLayoutDirectories() throws Exception {
        assertTrue(ResourceLoader.isLayoutDirectory(File.separator + "layout"));
    }

    @Test
    public void shouldLoadSystemResources() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("assets"));
        String stringValue = resourceLoader.getStringValue(android.R.string.copy);
        assertEquals("Copy", stringValue);

        ViewLoader.ViewNode node = resourceLoader.getLayoutViewNode("android:layout/simple_spinner_item");
        assertNotNull(node);
    }

    @Test
    public void shouldLoadLocalResources() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("assets"));
        String stringValue = resourceLoader.getStringValue(R.string.copy);
        assertEquals("Local Copy", stringValue);
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateView() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("layout"));
        resourceLoader.setStrictI18n(true);
        ViewGroup vg = new FrameLayout(Robolectric.application);
    	resourceLoader.inflateView(Robolectric.application, R.layout.text_views, vg);
    }
    
    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateMenu() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("menu"));
        resourceLoader.setStrictI18n(true);
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test, null);
    }
    
    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflatePreferences() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("xml"));
        resourceLoader.setStrictI18n(true);
    	resourceLoader.inflatePreferences(Robolectric.application, R.xml.preferences);
    }

    @Test
    public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("layout"));
        resourceLoader.setLayoutQualifierSearchPath("does-not-exist", "land", "xlarge");
        ViewGroup viewGroup = new FrameLayout(Robolectric.application);
        ViewGroup view = (ViewGroup) resourceLoader.inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("land"));
    }

    @Test
    public void checkForPollution1() throws Exception {
        checkForPollutionHelper();
    }

    @Test
    public void checkForPollution2() throws Exception {
        checkForPollutionHelper();
    }

    private void checkForPollutionHelper() {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        ViewGroup viewGroup = new FrameLayout(Robolectric.application);
        ViewGroup view = (ViewGroup) resourceLoader.inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("default"));
        resourceLoader.setLayoutQualifierSearchPath("land"); // testing if this pollutes the other test
    }
    
    @Test
    public void shouldIdentifyNinePatchDrawables() {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();

        assertThat(resourceLoader.isNinePatchDrawable(R.drawable.nine_patch_drawable), equalTo(true));
        assertThat(resourceLoader.isNinePatchDrawable(R.drawable.l2_yellow), equalTo(false));
        assertThat(resourceLoader.isNinePatchDrawable(R.drawable.state_drawable), equalTo(false));
        assertThat(resourceLoader.isNinePatchDrawable(R.drawable.animation_list), equalTo(false));
        assertThat(resourceLoader.isNinePatchDrawable(0), equalTo(false));
        assertThat(resourceLoader.isNinePatchDrawable(-1), equalTo(false));
    }
}
