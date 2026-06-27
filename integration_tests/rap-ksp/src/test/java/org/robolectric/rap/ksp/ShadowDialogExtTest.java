package org.robolectric.rap.ksp;

import static com.google.common.truth.Truth.assertThat;

import android.app.Dialog;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;

/**
 * Integration tests verifying that a KSP-processed shadow with {@code shadowPicker} attribute is
 * correctly placed in {@code SHADOW_PICKER_MAP} and the picker is invoked at runtime.
 */
@RunWith(RobolectricTestRunner.class)
public final class ShadowDialogExtTest {

  @Test
  public void shadowPicker_shadowIsExtracted() {
    Context context = RuntimeEnvironment.getApplication();
    Dialog dialog = new Dialog(context);
    ShadowDialogExt shadow = Shadow.extract(dialog);
    assertThat(shadow).isNotNull();
  }

  @Test
  public void shadowPicker_shadowIsCorrectType() {
    Context context = RuntimeEnvironment.getApplication();
    Dialog dialog = new Dialog(context);
    Object shadow = Shadow.extract(dialog);
    assertThat(shadow).isInstanceOf(ShadowDialogExt.class);
  }
}
