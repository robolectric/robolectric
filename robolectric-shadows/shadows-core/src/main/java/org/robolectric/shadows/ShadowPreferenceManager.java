package org.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.preference.PreferenceManager}.
 */
@Implements(PreferenceManager.class)
public class ShadowPreferenceManager {

  @Implementation
  public static SharedPreferences getDefaultSharedPreferences(Context context) {
    return ShadowApplication.getInstance().getSharedPreferences("__default__", Context.MODE_PRIVATE);
  }
}
