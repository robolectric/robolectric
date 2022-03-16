package android.graphics;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.graphics.Matrix.ScaleToFit;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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

  @Test
  public void mapPointsNull() {
    assertThrows(Exception.class, () -> new Matrix().mapPoints(null));
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

  @Test
  public void mapPointsArraysMismatch() {
    assertThrows(Exception.class, () -> new Matrix().mapPoints(new float[8], new float[9]));
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

  @Test
  public void mapPointsWithIndicesNull() {
    assertThrows(Exception.class, () -> new Matrix().mapPoints(null, 0, new float[9], 0, 1));
  }

  @Test
  public void setRectToRect() {
    RectF r1 = new RectF();
    r1.set(1f, 2f, 3f, 3f);
    RectF r2 = new RectF();
    r1.set(10f, 20f, 30f, 30f);
    Matrix matrix = new Matrix();
    float[] result = new float[9];

    assertThat(matrix.setRectToRect(r1, r2, ScaleToFit.CENTER)).isTrue();
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    matrix.setRectToRect(r1, r2, ScaleToFit.END);
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    matrix.setRectToRect(r1, r2, ScaleToFit.FILL);
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    matrix.setRectToRect(r1, r2, ScaleToFit.START);
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    assertThat(matrix.setRectToRect(r2, r1, ScaleToFit.CENTER)).isFalse();
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    assertThat(matrix.setRectToRect(r2, r1, ScaleToFit.FILL)).isFalse();
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    assertThat(matrix.setRectToRect(r2, r1, ScaleToFit.START)).isFalse();
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f});

    assertThat(matrix.setRectToRect(r2, r1, ScaleToFit.END)).isFalse();
    matrix.getValues(result);
    assertThat(result)
        .isEqualTo(new float[] {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f});
  }

  @Test
  public void testSetRectToRectNull() {
    assertThrows(Exception.class, () -> new Matrix().setRectToRect(null, null, ScaleToFit.CENTER));
  }
}
