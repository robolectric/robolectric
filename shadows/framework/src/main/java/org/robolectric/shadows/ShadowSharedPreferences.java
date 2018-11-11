package org.robolectric.shadows;

import android.app.QueuedWork;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Dummy container class for nested shadow class */
public class ShadowSharedPreferences {

  @Implements(
      className = "android.app.SharedPreferencesImpl$EditorImpl",
      minSdk = VERSION_CODES.O,
      isInAndroidSdk = false)
  public static class ShadowSharedPreferencesEditorImpl {

    @RealObject Object realObject;

    @Implementation
    protected void apply() {
      Shadow.directlyOn(realObject, "android.app.SharedPreferencesImpl$EditorImpl", "apply");
      // Flush QueuedWork. This resolves the deadlock of calling 'apply' followed by 'commit'.
      QueuedWork.waitToFinish();
    }
  }
}
