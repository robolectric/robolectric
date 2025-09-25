package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public final class ShadowRenderScriptTest {

  RenderScript renderScript = RenderScript.create(ApplicationProvider.getApplicationContext());

  @Test
  public void create_scriptIntrinsicBlur() {
    Element element = Element.U8_4(renderScript);
    assertThat((long) ReflectionHelpers.getField(element, "mID")).isNotEqualTo(0);
    assertThat(ScriptIntrinsicBlur.create(renderScript, element)).isNotNull();
  }

  @Test
  public void allocation_createFromBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Element rgbElement = Element.RGBA_8888(renderScript);
    assertThat((long) ReflectionHelpers.getField(rgbElement, "mID")).isNotEqualTo(0);
    ScriptIntrinsicBlur blurScript =
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
    Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);
    assertThat(allocation).isNotNull();
    blurScript.setInput(allocation);
  }

  @Test
  public void allocation_createTyped() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Allocation input =
        Allocation.createFromBitmap(
            renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
    Allocation allocationOutput = Allocation.createTyped(renderScript, input.getType());
    assertThat(allocationOutput).isNotNull();
  }

  @Test
  public void isDestroyed() {
    RenderScript renderScript = RenderScript.create(ApplicationProvider.getApplicationContext());
    assertThat(shadowOf(renderScript).isDestroyed()).isFalse();
    renderScript.destroy();
    assertThat(shadowOf(renderScript).isDestroyed()).isTrue();
  }
}
