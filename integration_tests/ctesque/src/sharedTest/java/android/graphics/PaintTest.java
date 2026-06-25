package android.graphics;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link Paint} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class PaintTest {
  @Test
  public void testStrokeCapDefaults() {
    Paint paint = new Paint();
    assertThat(paint.getStrokeCap()).isEqualTo(Paint.Cap.BUTT);
    assertThat(paint.getStrokeJoin()).isEqualTo(Paint.Join.MITER);
    assertThat(paint.getStyle()).isEqualTo(Paint.Style.FILL);
  }
}
