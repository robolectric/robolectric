package com.xtremelabs.robolectric.res;


import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.annotation.Values;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.util.I18nException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class ResourceLoaderTest {

    private ResourcePath resourcePath;
    private ResourcePath systemResourcePath;

    @Before
    public void setUp() throws Exception {
        resourcePath = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
        systemResourcePath = AndroidResourcePathFinder.getSystemResourcePath(Robolectric.DEFAULT_SDK_VERSION, resourcePath);
    }

    @Test
    public void shouldLoadSystemResources() throws Exception {
        PackageResourceLoader resourceLoader = new PackageResourceLoader(resourcePath, systemResourcePath);
        String stringValue = resourceLoader.getStringValue(android.R.string.copy, "");
        assertEquals("Copy", stringValue);

        ViewNode node = resourceLoader.getLayoutViewNode(new ResName("android:layout/simple_spinner_item"), "");
        assertNotNull(node);
    }

    @Test
    public void shouldLoadLocalResources() throws Exception {
        ResourceLoader resourceLoader = new PackageResourceLoader(resourcePath);
        String stringValue = resourceLoader.getStringValue(R.string.copy, "");
        assertEquals("Local Copy", stringValue);
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateView() throws Exception {
        shadowOf(Robolectric.application).setStrictI18n(true);
        ResourceLoader resourceLoader = shadowOf(Robolectric.application).getResourceLoader();
        ViewGroup vg = new FrameLayout(Robolectric.application);
        new RoboLayoutInflater(resourceLoader).inflateView(Robolectric.application, R.layout.text_views, vg, "");
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflatePreferences() throws Exception {
        shadowOf(Robolectric.application).setStrictI18n(true);
        ResourceLoader resourceLoader = shadowOf(Robolectric.application).getResourceLoader();
    	resourceLoader.inflatePreferences(Robolectric.application, R.xml.preferences);
    }

    @Test @Values(qualifiers = "doesnotexist-land-xlarge")
    public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        ViewGroup viewGroup = new FrameLayout(Robolectric.application);
        ViewGroup view = (ViewGroup) new RoboLayoutInflater(resourceLoader).inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup, "doesnotexist-land-xlarge");
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
        ViewGroup view = (ViewGroup) new RoboLayoutInflater(resourceLoader).inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup, "");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("default"));
        Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).overrideQualifiers("land"); // testing if this pollutes the other test
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

    @Test
    public void testStringsAreResolved() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(Arrays.asList(resourceLoader.getStringArrayValue(R.array.items, "")), hasItems("foo", "bar"));
    }

    @Test
    public void testStringsAreWithReferences() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(Arrays.asList(resourceLoader.getStringArrayValue(R.array.greetings, "")), hasItems("hola", "Hello"));
    }

    @Test
    public void shouldAddAndroidToSystemStringArrayName() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(Arrays.asList(resourceLoader.getStringArrayValue(android.R.array.emailAddressTypes, "")), hasItems("Home", "Work", "Other", "Custom"));
        assertThat(Arrays.asList(resourceLoader.getStringArrayValue(R.array.emailAddressTypes, "")), hasItems("Doggy", "Catty"));
    }

    @Test
    public void testIntegersAreResolved() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(R.array.zero_to_four_int_array, ""),
                equalTo(new int[]{0, 1, 2, 3, 4}));
    }

    @Test
    public void testEmptyArray() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(R.array.empty_int_array, "").length,
                equalTo(0));
    }

    @Test
    public void testIntegersWithReferences() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(R.array.with_references_int_array, ""),
                equalTo(new int[]{0, 2000, 1}));
    }

    @Test public void shouldLoadForAllQualifiers() throws Exception {
        ResourceLoader resourceLoader = new PackageResourceLoader(resourcePath);
        assertThat(resourceLoader.getStringValue(R.string.hello, ""), equalTo("Hello"));
        assertThat(resourceLoader.getStringValue(R.string.hello, "fr"), equalTo("Bonjour"));
    }
}
