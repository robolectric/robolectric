package com.xtremelabs.robolectric.res;

import android.app.Activity;
import android.preference.*;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.util.I18nException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceLoaderTest {
    private PreferenceLoader prefLoader;

    @Before
    public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor(testResources(), systemResources());
        prefLoader = new PreferenceLoader(resourceExtractor);
        new DocumentLoader(prefLoader).loadResourceXmlDir(testResources(), "xml");
    }

    @Test
    public void shouldCreateCorrectClasses() {
        PreferenceScreen screen = prefLoader.inflatePreferences(new Activity(), TEST_PACKAGE + ":xml/preferences");
        assertThatScreenMatchesExpected(screen);
    }

    @Test
    public void shouldLoadByResourceId() {
       PreferenceScreen screen = prefLoader.inflatePreferences(new Activity(), R.xml.preferences);
       assertThatScreenMatchesExpected(screen);
    }

    @Test(expected=I18nException.class)
    public void shouldThrowI18nExceptionOnPrefsWithBareStrings() throws Exception {
        Robolectric.getShadowApplication().setStrictI18n(true);
        ResourceExtractor resourceExtractor = new ResourceExtractor(testResources());

        prefLoader = new PreferenceLoader(resourceExtractor);
        new DocumentLoader(prefLoader).loadResourceXmlDir(testResources(), "xml");

        prefLoader.inflatePreferences(Robolectric.application, R.xml.preferences);
    }

    protected void assertThatScreenMatchesExpected(PreferenceScreen screen) {
        assertThat(screen.getPreferenceCount(), equalTo(7));

        assertThat(screen.getPreference(0), instanceOf(PreferenceCategory.class));
        assertThat(((PreferenceCategory)screen.getPreference(0)).getPreference(0), instanceOf(Preference.class));

        PreferenceScreen innerScreen = (PreferenceScreen) screen.getPreference(1);
        assertThat(innerScreen, instanceOf(PreferenceScreen.class));
        assertThat(innerScreen.getKey().toString(), is("screen"));
        assertThat(innerScreen.getTitle().toString(), is("Screen Test"));
        assertThat(innerScreen.getSummary(), nullValue());
        assertThat(innerScreen.getPreference(0), instanceOf(Preference.class));

        assertThat(screen.getPreference(2), instanceOf(CheckBoxPreference.class));
        assertThat(screen.getPreference(3), instanceOf(EditTextPreference.class));
        assertThat(screen.getPreference(4), instanceOf(ListPreference.class));
        assertThat(screen.getPreference(5), instanceOf(Preference.class));
        assertThat(screen.getPreference(6), instanceOf(RingtonePreference.class));
    }
}
