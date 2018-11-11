package android.graphics;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link Matrix} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public final class MatrixTest {

  @Test
  public void mapRadius() throws Exception {
    Matrix matrix = new Matrix();

    assertThat(matrix.mapRadius(100f)).isEqualTo(100f);
    assertThat(matrix.mapRadius(Float.MAX_VALUE)).isEqualTo(Float.POSITIVE_INFINITY);
    assertThat(matrix.mapRadius(Float.MIN_VALUE)).isEqualTo(0f);

    matrix.postScale(2.0f, 2.0f);
    assertThat(matrix.mapRadius(1.0f)).isWithin(0.01f).of(2.0f);
  }
}
