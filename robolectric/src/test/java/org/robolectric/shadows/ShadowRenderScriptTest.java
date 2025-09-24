package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

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
  @Test
  public void renderScript_getContext_returnsContext() {
    RenderScript.create(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void create_scriptIntrinsicBlur() {
    RenderScript renderScript = RenderScript.create(ApplicationProvider.getApplicationContext());
    Element element = Element.U8_4(renderScript);
    assertThat((long) ReflectionHelpers.getField(element, "mID")).isNotEqualTo(0);
    assertThat(ScriptIntrinsicBlur.create(renderScript, element)).isNotNull();
  }

  @Test
  public void allocation_createFromBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    RenderScript renderScript = RenderScript.create(ApplicationProvider.getApplicationContext());
    Element rgbElement = Element.RGBA_8888(renderScript);
    assertThat((long) ReflectionHelpers.getField(rgbElement, "mID")).isNotEqualTo(0);
    ScriptIntrinsicBlur blurScript =
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
    Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);
    assertThat(allocation).isNotNull();
    blurScript.setInput(allocation);
  }
}
