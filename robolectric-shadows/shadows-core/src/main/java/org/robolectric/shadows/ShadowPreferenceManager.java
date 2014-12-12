package org.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PreferenceManager.class)
public class ShadowPreferenceManager {

  @Implementation
  public static SharedPreferences getDefaultSharedPreferences(Context context) {
    ShadowApplication shadowApplication = ShadowApplication.getInstance();
    return shadowApplication.getSharedPreferences("__default__", Context.MODE_PRIVATE);
  }
}
