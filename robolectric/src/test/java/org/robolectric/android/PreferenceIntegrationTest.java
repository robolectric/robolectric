package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class PreferenceIntegrationTest {

  @Test
  public void inflate_shouldCreateCorrectClasses() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    assertThat(screen.getPreference(0)).isInstanceOf(PreferenceCategory.class);

    PreferenceCategory category = (PreferenceCategory) screen.getPreference(0);
    assertThat(category.getPreference(0)).isInstanceOf(Preference.class);

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

  @Test
  public void inflate_shouldParseIntentContainedInPreference() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference intentPreference = screen.findPreference("intent");

    Intent intent = intentPreference.getIntent();
    assertThat(intent).isNotNull();
    assertThat(intent.getAction()).isEqualTo("action");
    assertThat(intent.getData()).isEqualTo(Uri.parse("tel://1235"));
    assertThat(intent.getType()).isEqualTo("application/text");
    assertThat(intent.getComponent().getClassName()).isEqualTo("org.robolectric.test.Intent");
    assertThat(intent.getComponent().getPackageName()).isEqualTo("org.robolectric");
  }

  @Test
  public void inflate_shouldBindPreferencesToPreferenceManager() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference preference = screen.findPreference("preference");
    assertThat(preference.getPreferenceManager().findPreference("preference")).isNotNull();
  }

  @Test
  public void setPersistent_shouldMarkThePreferenceAsPersistent() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference preference = screen.findPreference("preference");

    preference.setPersistent(true);
    assertThat(preference.isPersistent()).isTrue();

    preference.setPersistent(false);
    assertThat(preference.isPersistent()).isFalse();
  }

  @Test
  public void setEnabled_shouldEnableThePreference() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference preference = screen.findPreference("preference");

    preference.setEnabled(true);
    assertThat(preference.isEnabled()).isTrue();

    preference.setEnabled(false);
    assertThat(preference.isEnabled()).isFalse();
  }

  @Test
  public void setOrder_shouldSetOrderOnPreference() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference preference = screen.findPreference("preference");

    preference.setOrder(100);
    assertThat(preference.getOrder()).isEqualTo(100);

    preference.setOrder(50);
    assertThat(preference.getOrder()).isEqualTo(50);
  }

  @Test
  public void setDependency_shouldSetDependencyBetweenPreferences() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference dependant = screen.findPreference("dependant");
    assertThat(dependant.getDependency()).isEqualTo("preference");

    dependant.setDependency(null);
    assertThat(dependant.getDependency()).isNull();
  }

  @Test
  public void click_shouldCallPreferenceClickListener() throws Exception {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final Preference preference = screen.findPreference("preference");

    boolean[] holder = new boolean[1];
    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        holder[0] = true;
        return true;
      }
    });

    shadowOf(preference).click();
    assertThat(holder[0]).isTrue();
  }

  private PreferenceScreen inflatePreferenceActivity() {
    TestPreferenceActivity activity = Robolectric.setupActivity(TestPreferenceActivity.class);
    return activity.getPreferenceScreen();
  }

  @SuppressWarnings("FragmentInjection")
  private static class TestPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
  }
}
