package org.robolectric.rap.ksp

import android.app.Application
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.S
import org.robolectric.annotation.Implements
import org.robolectric.annotation.Resetter
import org.robolectric.shadows.ShadowApplication

/** This shadow tests the logic that emits resetters in KSP when a min/max sdk is specified. */
@Implements(value = Application::class, minSdk = P, maxSdk = S)
class ExtendedShadowApplication : ShadowApplication() {
  companion object {
    @JvmStatic @Resetter fun reset() {}
  }
}
