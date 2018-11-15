package org.robolectric.shadows;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowStaticLayoutTest {

  @Test
  public void generate_shouldNotThrowException() {
    new StaticLayout("Hello!", new TextPaint(), 100, Layout.Alignment.ALIGN_LEFT, 1.2f, 1.0f, true);
  }
}
