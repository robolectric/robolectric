package android.text.format;

import static android.os.Build.VERSION_CODES.P;

import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests that Robolectric's android.text.StaticLayout support is consistent with device. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class StaticLayoutTest {
  @Test
  @Config(minSdk = P)
  public void testStaticLayout() {
    StaticLayout.Builder.obtain("invalidEmail", 0, 12, new TextPaint(), 256)
        .build()
        .getPrimaryHorizontal(12);
  }
}
