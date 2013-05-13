package org.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.tester.android.content.TestSharedPreferences;

import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadow for {@code PreferenceManager} that returns instances of the {@link org.robolectric.tester.android.content.TestSharedPreferences} utility class
 */
@Implements(PreferenceManager.class)
public class ShadowPreferenceManager {

  @Implementation
  public static SharedPreferences getDefaultSharedPreferences(Context context) {
    ShadowApplication shadowApplication = shadowOf((Application) context.getApplicationContext());
    return new TestSharedPreferences(shadowApplication.getSharedPreferenceMap(), "__default__", Context.MODE_PRIVATE);
  }

}
