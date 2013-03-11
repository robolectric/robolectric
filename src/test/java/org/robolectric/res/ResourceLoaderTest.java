package org.robolectric.res;

import android.preference.PreferenceActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.LayoutBuilder;
import org.robolectric.util.I18nException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.TestUtil.resourceFile;

@RunWith(TestRunners.WithDefaults.class)
public class ResourceLoaderTest {
    private ResourcePath resourcePath;

    @Before
    public void setUp() throws Exception {
        resourcePath = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
    }

    @Test
    public void shouldLoadSystemResources() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        String stringValue = resourceLoader.getStringValue(resourceLoader.getResourceIndex().getResName(android.R.string.copy), "");
        assertEquals("Copy", stringValue);

        ViewNode node = resourceLoader.getLayoutViewNode(new ResName("android:layout/simple_spinner_item"), "");
        assertNotNull(node);
    }

    @Test
    public void shouldLoadLocalResources() throws Exception {
        ResourceLoader resourceLoader = new PackageResourceLoader(resourcePath);
        String stringValue = resourceLoader.getStringValue(resourceLoader.getResourceIndex().getResName(R.string.copy), "");
        assertEquals("Local Copy", stringValue);
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflateView() throws Exception {
        shadowOf(Robolectric.application).setStrictI18n(true);
        ResourceLoader resourceLoader = shadowOf(Robolectric.application).getResourceLoader();
        ViewGroup vg = new FrameLayout(Robolectric.application);
        new LayoutBuilder(resourceLoader).inflateView(Robolectric.application, R.layout.text_views, vg, "");
    }

    @Test(expected=I18nException.class)
    public void shouldThrowExceptionOnI18nStrictModeInflatePreferences() throws Exception {
        shadowOf(Robolectric.application).setStrictI18n(true);
        PreferenceActivity preferenceActivity = new PreferenceActivity() {
        };
        preferenceActivity.addPreferencesFromResource(R.xml.preferences);
    }

    @Test @Config(qualifiers = "doesnotexist-land-xlarge")
    public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        ViewGroup viewGroup = new FrameLayout(Robolectric.application);
        ViewGroup view = (ViewGroup) new LayoutBuilder(resourceLoader).inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup, "doesnotexist-land-xlarge");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString()).isEqualTo("land");
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
        ViewGroup view = (ViewGroup) new LayoutBuilder(resourceLoader).inflateView(Robolectric.application, R.layout.different_screen_sizes, viewGroup, "");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString()).isEqualTo("default");
        Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).overrideQualifiers("land"); // testing if this pollutes the other test
    }
    
    @Test
    public void testStringsAreResolved() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getStringArrayValue(resourceLoader.getResourceIndex().getResName(R.array.items), "")).containsExactly("foo", "bar");
    }

    @Test
    public void testStringsAreWithReferences() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getStringArrayValue(resourceLoader.getResourceIndex().getResName(R.array.greetings), "")).containsExactly("hola", "Hello");
    }

    @Test
    public void shouldAddAndroidToSystemStringArrayName() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getStringArrayValue(resourceLoader.getResourceIndex().getResName(android.R.array.emailAddressTypes), "")).containsExactly("Home", "Work", "Other", "Custom");
        assertThat(resourceLoader.getStringArrayValue(resourceLoader.getResourceIndex().getResName(R.array.emailAddressTypes), "")).containsExactly("Doggy", "Catty");
    }

    @Test
    public void testIntegersAreResolved() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(resourceLoader.getResourceIndex().getResName(R.array.zero_to_four_int_array), "")).isEqualTo(new int[]{0, 1, 2, 3, 4});
    }

    @Test
    public void testEmptyArray() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(resourceLoader.getResourceIndex().getResName(R.array.empty_int_array), "").length).isEqualTo(0);
    }

    @Test
    public void testIntegersWithReferences() throws Exception {
        ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
        assertThat(resourceLoader.getIntegerArrayValue(resourceLoader.getResourceIndex().getResName(R.array.with_references_int_array), "")).isEqualTo(new int[]{0, 2000, 1});
    }

    @Test public void shouldLoadForAllQualifiers() throws Exception {
        ResourceLoader resourceLoader = new PackageResourceLoader(resourcePath);
        assertThat(resourceLoader.getStringValue(resourceLoader.getResourceIndex().getResName(R.string.hello), "")).isEqualTo("Hello");
        assertThat(resourceLoader.getStringValue(resourceLoader.getResourceIndex().getResName(R.string.hello), "fr")).isEqualTo("Bonjour");
    }
}
