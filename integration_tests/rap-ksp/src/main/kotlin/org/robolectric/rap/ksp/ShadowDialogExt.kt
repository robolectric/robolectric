package org.robolectric.rap.ksp

import android.app.Dialog
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.ShadowPicker
import org.robolectric.shadows.ShadowDialog

/**
 * Tests the KSP processor's handling of the `shadowPicker` attribute in `@Implements`. When
 * `shadowPicker` is specified, the KSP processor puts the entry in `SHADOW_PICKER_MAP` instead of
 * the `SHADOWS` list. At runtime Robolectric invokes the picker to select the shadow.
 */
@Implements(value = Dialog::class, shadowPicker = ShadowDialogExt.Picker::class)
class ShadowDialogExt : ShadowDialog() {
  class Picker : ShadowPicker<ShadowDialogExt> {
    override fun pickShadowClass(): Class<ShadowDialogExt> = ShadowDialogExt::class.java
  }
}
