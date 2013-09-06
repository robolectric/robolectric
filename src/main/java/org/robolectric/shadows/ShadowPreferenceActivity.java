package org.robolectric.shadows;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.PreferenceNode;
import org.robolectric.res.ResName;
import org.robolectric.res.builder.PreferenceBuilder;
import org.robolectric.util.I18nException;

import static org.robolectric.Robolectric.shadowOf;

@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowActivity {
  private int preferencesResId = -1;
  private PreferenceScreen preferenceScreen;

  @Implementation
  public void onCreate(Bundle savedInstanceState) {
    realActivity.setContentView(android.R.layout.list_content);
  }

  @Implementation
  public void addPreferencesFromResource(int preferencesResId) {
    this.preferencesResId = preferencesResId;
    preferenceScreen = inflatePreferences(preferencesResId);
  }

  private PreferenceScreen inflatePreferences(int preferencesResId) {
    ResName resName = getResName(preferencesResId);
    String qualifiers = shadowOf(getResources().getConfiguration()).getQualifiers();
    PreferenceNode preferenceNode = getResourceLoader().getPreferenceNode(resName, qualifiers);
    try {
      return (PreferenceScreen) new PreferenceBuilder().inflate(preferenceNode, realActivity, null);
    } catch (I18nException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("error inflating " + resName, e);
    }
  }

  public int getPreferencesResId() {
    return preferencesResId;
  }

  @Implementation
  public PreferenceScreen getPreferenceScreen() {
    return preferenceScreen;
  }

  @Implementation
  public Preference findPreference(CharSequence key) {
    return preferenceScreen.findPreference(key);
  }
}
