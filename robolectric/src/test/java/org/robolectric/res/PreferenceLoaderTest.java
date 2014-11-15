package org.robolectric.res;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceLoaderTest {

  private ResBundle<PreferenceNode> resBundle;
  private PreferenceBuilder preferenceBuilder;

  @Before
  public void setUp() throws Exception {
    resBundle = new ResBundle<PreferenceNode>();
    PreferenceLoader prefLoader = new PreferenceLoader(resBundle);
    new DocumentLoader(testResources()).load("xml", prefLoader);

    preferenceBuilder = new PreferenceBuilder();
  }

  @Test
  public void shouldCreateCorrectClasses() {
    PreferenceNode preferenceNode = resBundle.get(new ResName(TEST_PACKAGE + ":xml/preferences"), "");
    PreferenceScreen screen = (PreferenceScreen) preferenceBuilder.inflate(preferenceNode, Robolectric.setupActivity(Activity.class), null);
    assertThatScreenMatchesExpected(screen);
  }

  @Test
  public void shouldSetContextInScreens() {
    PreferenceNode preferenceNode = resBundle.get(new ResName(TEST_PACKAGE + ":xml/preferences"), "");
    Activity activity = Robolectric.setupActivity(Activity.class);
    PreferenceScreen screen = (PreferenceScreen) preferenceBuilder.inflate(preferenceNode, activity, null);

    assertThat(screen.getContext()).isEqualTo(activity);

    PreferenceScreen innerScreen = (PreferenceScreen) screen.getPreference(1);
    assertThat(innerScreen.getContext()).isEqualTo(activity);
  }

  @Test
  public void shouldParseIntentContainedInPreference() throws Exception {
    PreferenceNode preferenceNode = resBundle.get(new ResName(TEST_PACKAGE + ":xml/intent_preference"), "");
    PreferenceScreen screen = (PreferenceScreen) preferenceBuilder.inflate(preferenceNode, Robolectric.setupActivity(Activity.class), null);

    assertThat(screen.getPreferenceCount()).isEqualTo(1);
    Preference intentPreference = screen.getPreference(0);
    Intent intent = intentPreference.getIntent();
    assertThat(intent).isNotNull();
    assertThat(intent.getAction()).isEqualTo("action");
    assertThat(intent.getData()).isEqualTo(Uri.parse("tel://1235"));
    assertThat(intent.getType()).isEqualTo("application/text");
    assertThat(intent.getComponent().getClassName()).isEqualTo("org.robolectric.test.Intent");
    assertThat(intent.getComponent().getPackageName()).isEqualTo("org.robolectric");
  }

  protected void assertThatScreenMatchesExpected(PreferenceScreen screen) {
    assertThat(screen.getPreferenceCount()).isEqualTo(8);

    assertThat(screen.getPreference(0)).isInstanceOf(PreferenceCategory.class);
    assertThat(((PreferenceCategory) screen.getPreference(0)).getPreference(0)).isInstanceOf(Preference.class);

    PreferenceScreen innerScreen = (PreferenceScreen) screen.getPreference(1);
    assertThat(innerScreen).isInstanceOf(PreferenceScreen.class);
    assertThat(innerScreen.getKey()).isEqualTo("screen");
    assertThat(innerScreen.getTitle().toString()).isEqualTo("Screen Test");
    assertThat(innerScreen.getSummary()).isEqualTo("Screen summary");
    assertThat(innerScreen.getPreference(0)).isInstanceOf(Preference.class);

    assertThat(screen.getPreference(2)).isInstanceOf(CheckBoxPreference.class);
    assertThat(screen.getPreference(3)).isInstanceOf(EditTextPreference.class);
    assertThat(screen.getPreference(4)).isInstanceOf(ListPreference.class);
    assertThat(screen.getPreference(5)).isInstanceOf(Preference.class);
    assertThat(screen.getPreference(6)).isInstanceOf(RingtonePreference.class);
    assertThat(screen.getPreference(7)).isInstanceOf(Preference.class);
  }
}
