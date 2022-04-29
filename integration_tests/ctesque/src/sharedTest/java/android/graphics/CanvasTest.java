package android.graphics;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility tests for {@link Canvas} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class CanvasTest {
  @Test
  public void getClipBounds_emptyClip() {
    Canvas canvas = new Canvas();
    Rect r = canvas.getClipBounds();
    assertThat(r).isEqualTo(new Rect(0, 0, 0, 0));
    assertThat(canvas.getClipBounds(new Rect())).isFalse();
  }

  @Test
  public void getClipBounds_backingBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Rect r = canvas.getClipBounds();
    assertThat(r).isEqualTo(new Rect(0, 0, 100, 100));
    assertThat(canvas.getClipBounds(new Rect())).isTrue();
  }
}
