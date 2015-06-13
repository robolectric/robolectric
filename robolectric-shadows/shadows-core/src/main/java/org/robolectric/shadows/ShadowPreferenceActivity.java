package org.robolectric.shadows;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.PreferenceNode;
import org.robolectric.res.ResName;
import org.robolectric.shadows.util.PreferenceBuilder;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.preference.PreferenceActivity}.
 */
@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowActivity {
  private int preferencesResId = -1;
  private PreferenceScreen preferenceScreen;
  private final PreferenceBuilder preferenceBuilder = new PreferenceBuilder();

  @Implementation
  public void addPreferencesFromResource(int preferencesResId) {
    this.preferencesResId = preferencesResId;
    preferenceScreen = inflatePreferences(preferencesResId);
    ((PreferenceActivity)realActivity).setPreferenceScreen(preferenceScreen);
  }

  private PreferenceScreen inflatePreferences(int preferencesResId) {
    ResName resName = getResName(preferencesResId);
    String qualifiers = shadowOf(getResources().getConfiguration()).getQualifiers();
    PreferenceNode preferenceNode = getResourceLoader().getPreferenceNode(resName, qualifiers);
    try {
      return (PreferenceScreen) preferenceBuilder.inflate(preferenceNode, realActivity, null);
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
