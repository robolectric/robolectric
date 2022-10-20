package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.preference.Preference;
import android.preference.PreferenceManager;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.ForType;

@Implements(Preference.class)
public class ShadowPreference {
  @RealObject private Preference realPreference;

  public void callOnAttachedToHierarchy(PreferenceManager preferenceManager) {
    reflector(PreferenceReflector.class, realPreference).onAttachedToHierarchy(preferenceManager);
  }

  public boolean click() {
    return realPreference.getOnPreferenceClickListener().onPreferenceClick(realPreference);
  }

  @ForType(Preference.class)
  interface PreferenceReflector {
    void onAttachedToHierarchy(PreferenceManager preferenceManager);
  }
}
