package org.robolectric.shadows;

import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.app.QueuedWork;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Placeholder container class for nested shadow class */
public class ShadowSharedPreferences {

  @Implements(
      className = "android.app.SharedPreferencesImpl$EditorImpl",
      minSdk = VERSION_CODES.O,
      isInAndroidSdk = false)
  public static class ShadowSharedPreferencesEditorImpl {

    @RealObject Object realObject;
    private static final Object lock = new Object();

    @Implementation
    protected void apply() {
      if (ShadowLooper.looperMode() == LEGACY) {
        synchronized (lock) {
          Shadow.directlyOn(realObject, "android.app.SharedPreferencesImpl$EditorImpl", "apply");
          // Flush QueuedWork. This resolves the deadlock of calling 'apply' followed by 'commit'.
          QueuedWork.waitToFinish();
        }
      } else {
        Shadow.directlyOn(realObject, "android.app.SharedPreferencesImpl$EditorImpl", "apply");
        // Flush QueuedWork. This resolves the deadlock of calling 'apply' followed by 'commit'.
        QueuedWork.waitToFinish();
      }
    }

    @Implementation
    protected boolean commit() {
      // In Legacy LooperMode, all Android loopers run on a single thread.
      // This lock resolves the deadlock of when the main thread/looper is blocked until
      // 'commit' finishes, but QueuedWork is blocked until the main looper is unblocked.
      if (ShadowLooper.looperMode() == LEGACY) {
        synchronized (lock) {
          return Shadow.directlyOn(
              realObject, "android.app.SharedPreferencesImpl$EditorImpl", "commit");
        }
      } else {
        return Shadow.directlyOn(
            realObject, "android.app.SharedPreferencesImpl$EditorImpl", "commit");
      }
    }
  }
}
