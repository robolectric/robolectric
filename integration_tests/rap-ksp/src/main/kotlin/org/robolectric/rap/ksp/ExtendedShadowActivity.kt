package org.robolectric.rap.ksp

import org.robolectric.annotation.Implements
import org.robolectric.annotation.Resetter
import org.robolectric.shadows.ShadowActivity

/**
 * Tests the KSP processor's handling of:
 * - `className` string attribute in `@Implements` (vs `value` class reference)
 * - `isInAndroidSdk = false` attribute (shadow is still registered in SHADOWS list)
 * - Companion object `@Resetter` without SDK bounds (unguarded resetter)
 */
@Implements(className = "android.app.Activity", isInAndroidSdk = false)
class ExtendedShadowActivity : ShadowActivity() {
  companion object {
    @JvmStatic @Resetter fun reset() {}
  }
}
