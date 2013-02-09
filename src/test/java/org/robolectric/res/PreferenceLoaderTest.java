package org.robolectric.res;

import android.app.Activity;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.builder.PreferenceBuilder;
import org.robolectric.util.I18nException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceLoaderTest {
    private PreferenceLoader prefLoader;
    private ResBundle<PreferenceNode> resBundle;
    private PreferenceBuilder preferenceBuilder;

    @Before
    public void setUp() throws Exception {
        resBundle = new ResBundle<PreferenceNode>();
        prefLoader = new PreferenceLoader(resBundle);
        new DocumentLoader(prefLoader).loadResourceXmlDir(testResources(), "xml");

        preferenceBuilder = new PreferenceBuilder();
    }

    @Test
    public void shouldCreateCorrectClasses() {
        PreferenceNode preferenceNode = resBundle.get(new ResName(TEST_PACKAGE + ":xml/preferences"), "");
        PreferenceScreen screen = (PreferenceScreen) preferenceBuilder.inflate(preferenceNode, new Activity(), null);
        assertThatScreenMatchesExpected(screen);
    }

    @Test(expected = I18nException.class)
    public void shouldThrowI18nExceptionOnPrefsWithBareStrings() throws Exception {
        Robolectric.getShadowApplication().setStrictI18n(true);
        PreferenceNode preferenceNode = resBundle.get(new ResName(TEST_PACKAGE + ":xml/preferences"), "");
        preferenceBuilder.inflate(preferenceNode, Robolectric.application, null);
    }

    protected void assertThatScreenMatchesExpected(PreferenceScreen screen) {
        assertThat(screen.getPreferenceCount(), equalTo(7));

        assertThat(screen.getPreference(0), instanceOf(PreferenceCategory.class));
        assertThat(((PreferenceCategory) screen.getPreference(0)).getPreference(0), instanceOf(Preference.class));

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
