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

  @Test
  public void mapPoints() {
    float[] value = new float[9];
    value[0] = 100f;
    new Matrix().mapPoints(value);
    assertThat(value[0]).isEqualTo(100f);
  }

  @Test(expected = Exception.class)
  public void mapPointsNull() {
    new Matrix().mapPoints(null);
  }

  @Test
  public void mapPoints2() {
    float[] dst = new float[9];
    dst[0] = 100f;
    float[] src = new float[9];
    src[0] = 200f;
    new Matrix().mapPoints(dst, src);
    assertThat(dst[0]).isEqualTo(200f);
  }

  @Test(expected = Exception.class)
  public void mapPointsArraysMismatch() {
    new Matrix().mapPoints(new float[8], new float[9]);
  }

  @Test
  public void mapPointsWithIndices() {
    float[] dst = new float[9];
    dst[0] = 100f;
    float[] src = new float[9];
    src[0] = 200f;
    new Matrix().mapPoints(dst, 0, src, 0, 9 >> 1);
    assertThat(dst[0]).isEqualTo(200f);
  }

  @Test(expected = Exception.class)
  public void mapPointsWithIndicesNull() {
    new Matrix().mapPoints(null, 0, new float[9], 0, 1);
  }
}
