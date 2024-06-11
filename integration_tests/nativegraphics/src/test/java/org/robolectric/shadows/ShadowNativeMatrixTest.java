package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeMatrixTest {
  private static final float EPSILON = 1e-7f;

  @Test
  public void testIsIdentity() {
    final Matrix matrix = new Matrix();
    assertThat(matrix.isIdentity()).isTrue();

    matrix.postScale(2.0f, 2.0f);
    assertThat(matrix.isIdentity()).isFalse();
  }

  @Test
  public void testIsAffine() {
    final Matrix matrix = new Matrix();
    assertThat(matrix.isAffine()).isTrue();

    matrix.postScale(2.0f, 2.0f);
    assertThat(matrix.isAffine()).isTrue();
    matrix.postTranslate(1.0f, 2.0f);
    assertThat(matrix.isAffine()).isTrue();
    matrix.postRotate(45.0f);
    assertThat(matrix.isAffine()).isTrue();

    matrix.setValues(new float[] {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 2.0f});
    assertThat(matrix.isAffine()).isFalse();
  }

  @Test
  public void testRectStaysRect() {
    final Matrix matrix = new Matrix();
    assertThat(matrix.rectStaysRect()).isTrue();

    matrix.postScale(2.0f, 2.0f);
    assertThat(matrix.rectStaysRect()).isTrue();
    matrix.postTranslate(1.0f, 2.0f);
    assertThat(matrix.rectStaysRect()).isTrue();
    matrix.postRotate(45.0f);
    assertThat(matrix.rectStaysRect()).isFalse();
    matrix.postRotate(45.0f);
    assertThat(matrix.rectStaysRect()).isTrue();
  }

  @Test
  public void testGetSetValues() {
    final Matrix matrix = new Matrix();
    final float[] values = {0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f};
    matrix.setValues(values);
    final float[] matrixValues = new float[9];
    matrix.getValues(matrixValues);
    assertThat(matrixValues).isEqualTo(values);
  }

  @Test
  public void testSet() {
    final Matrix matrix1 = new Matrix();
    matrix1.postScale(2.0f, 2.0f);
    matrix1.postTranslate(1.0f, 2.0f);
    matrix1.postRotate(45.0f);

    final Matrix matrix2 = new Matrix();
    matrix2.set(matrix1);
    assertThat(matrix1).isEqualTo(matrix2);

    matrix2.set(null);
    assertThat(matrix2.isIdentity()).isTrue();
  }

  @Test
  public void testReset() {
    final Matrix matrix = new Matrix();
    matrix.postScale(2.0f, 2.0f);
    matrix.postTranslate(1.0f, 2.0f);
    matrix.postRotate(45.0f);
    matrix.reset();
    assertThat(matrix.isIdentity()).isTrue();
  }

  @Test
  public void testSetTranslate() {
    final Matrix matrix = new Matrix();
    matrix.setTranslate(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix, 1.0f, 1.0f), new PointF(3.0f, 3.0f));
    matrix.setTranslate(-2.0f, -2.0f);
    assertPointsEqual(mapPoint(matrix, 1.0f, 1.0f), new PointF(-1.0f, -1.0f));
  }

  @Test
  public void testPostTranslate() {
    final Matrix matrix1 = new Matrix();
    matrix1.postTranslate(1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(2.0f, 2.0f));

    matrix1.postTranslate(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(4.0f, 4.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.setScale(2.0f, 2.0f);
    matrix2.postTranslate(-5.0f, 10.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(-3.0f, 12.0f));
  }

  @Test
  public void testPreTranslate() {
    final Matrix matrix1 = new Matrix();
    matrix1.preTranslate(1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(2.0f, 2.0f));

    matrix1.preTranslate(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(4.0f, 4.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.setScale(2.0f, 2.0f);
    matrix2.preTranslate(-5.0f, 10.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(-8.0f, 22.0f));
  }

  @Test
  public void testSetScale() {
    final Matrix matrix = new Matrix();
    matrix.setScale(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix, 1.0f, 1.0f), new PointF(2.0f, 2.0f));
    matrix.setScale(-2.0f, -3.0f);
    assertPointsEqual(mapPoint(matrix, 2.0f, 3.0f), new PointF(-4.0f, -9.0f));
    matrix.setScale(-2.0f, -3.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 2.0f, 3.0f), new PointF(-1.0f, -5.0f));
  }

  @Test
  public void testPostScale() {
    final Matrix matrix1 = new Matrix();
    matrix1.postScale(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(2.0f, 2.0f));

    matrix1.postScale(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(4.0f, 4.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.postScale(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(1.0f, 1.0f));

    matrix2.setTranslate(1.0f, 2.0f);
    matrix2.postScale(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(3.0f, 5.0f));
  }

  @Test
  public void testPreScale() {
    final Matrix matrix1 = new Matrix();
    matrix1.preScale(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(2.0f, 2.0f));

    matrix1.preScale(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(4.0f, 4.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.preScale(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(1.0f, 1.0f));

    matrix2.setTranslate(1.0f, 2.0f);
    matrix2.preScale(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(2.0f, 3.0f));
  }

  @Test
  public void testSetRotate() {
    final Matrix matrix = new Matrix();
    matrix.setRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(-1.0f, 0.0f));
    matrix.setRotate(180.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, -1.0f));
    matrix.setRotate(270.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(1.0f, 0.0f));
    matrix.setRotate(360.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));

    matrix.setRotate(45.0f, 0.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));
  }

  @Test
  public void testPostRotate() {
    final Matrix matrix = new Matrix();
    matrix.postRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(-1.0f, 0.0f));
    matrix.postRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, -1.0f));
    matrix.postRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(1.0f, 0.0f));
    matrix.postRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));

    matrix.setTranslate(1.0f, 2.0f);
    matrix.postRotate(45.0f, 0.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(-0.70710677f, 3.1213202f));
  }

  @Test
  public void testPreRotate() {
    final Matrix matrix = new Matrix();
    matrix.preRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(-1.0f, 0.0f));
    matrix.preRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, -1.0f));
    matrix.preRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(1.0f, 0.0f));
    matrix.preRotate(90.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));

    matrix.setTranslate(1.0f, 2.0f);
    matrix.preRotate(45.0f, 0.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(1.0f, 3.0f));
  }

  @Test
  public void testSetSinCos() {
    final Matrix matrix = new Matrix();
    matrix.setSinCos(1.0f, 0.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(-1.0f, 0.0f));
    matrix.setSinCos(0.0f, -1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, -1.0f));
    matrix.setSinCos(-1.0f, 0.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(1.0f, 0.0f));
    matrix.setSinCos(0.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));

    final float sinCos = (float) Math.sqrt(2) / 2;
    matrix.setSinCos(sinCos, sinCos, 0.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 0.0f, 1.0f), new PointF(0.0f, 1.0f));
  }

  @Test
  public void testSetSkew() {
    final Matrix matrix = new Matrix();
    matrix.setSkew(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix, 1.0f, 1.0f), new PointF(3.0f, 3.0f));
    matrix.setSkew(-2.0f, -3.0f);
    assertPointsEqual(mapPoint(matrix, 2.0f, 3.0f), new PointF(-4.0f, -3.0f));
    matrix.setSkew(-2.0f, -3.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix, 2.0f, 3.0f), new PointF(-2.0f, 0.0f));
  }

  @Test
  public void testPostSkew() {
    final Matrix matrix1 = new Matrix();
    matrix1.postSkew(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(3.0f, 3.0f));

    matrix1.postSkew(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(9.0f, 9.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.postSkew(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(1.0f, 1.0f));

    matrix2.setTranslate(1.0f, 2.0f);
    matrix2.postSkew(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(6.0f, 5.0f));
  }

  @Test
  public void testPreSkew() {
    final Matrix matrix1 = new Matrix();
    matrix1.preSkew(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(3.0f, 3.0f));

    matrix1.preSkew(2.0f, 2.0f);
    assertPointsEqual(mapPoint(matrix1, 1.0f, 1.0f), new PointF(9.0f, 9.0f));

    final Matrix matrix2 = new Matrix();
    matrix2.preSkew(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(1.0f, 1.0f));

    matrix2.setTranslate(1.0f, 2.0f);
    matrix2.preSkew(2.0f, 2.0f, 1.0f, 1.0f);
    assertPointsEqual(mapPoint(matrix2, 1.0f, 1.0f), new PointF(2.0f, 3.0f));
  }

  @Test
  public void testSetConcat() {
    final Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(2.0f, 3.0f);
    final Matrix translateMatrix = new Matrix();
    translateMatrix.postTranslate(5.0f, 7.0f);
    final Matrix matrix = new Matrix();
    matrix.setConcat(translateMatrix, scaleMatrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(9.0f, 13.0f));

    final Matrix rotateMatrix = new Matrix();
    rotateMatrix.postRotate(90.0f);
    matrix.setConcat(rotateMatrix, matrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(-13.0f, 9.0f));
  }

  @Test
  public void testPostConcat() {
    final Matrix matrix = new Matrix();
    matrix.postScale(2.0f, 3.0f);
    final Matrix translateMatrix = new Matrix();
    translateMatrix.postTranslate(5.0f, 7.0f);
    matrix.postConcat(translateMatrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(9.0f, 13.0f));

    final Matrix rotateMatrix = new Matrix();
    rotateMatrix.postRotate(90.0f);
    matrix.postConcat(rotateMatrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(-13.0f, 9.0f));
  }

  @Test
  public void testPreConcat() {
    final Matrix matrix = new Matrix();
    matrix.preScale(2.0f, 3.0f);
    final Matrix translateMatrix = new Matrix();
    translateMatrix.setTranslate(5.0f, 7.0f);
    matrix.preConcat(translateMatrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(14.0f, 27.0f));

    final Matrix rotateMatrix = new Matrix();
    rotateMatrix.setRotate(90.0f);
    matrix.preConcat(rotateMatrix);
    assertPointsEqual(mapPoint(matrix, 2.0f, 2.0f), new PointF(6.0f, 27.0f));
  }

  @Test
  public void testInvert() {
    final Matrix matrix = new Matrix();
    final Matrix inverse = new Matrix();
    matrix.setScale(0.0f, 1.0f);
    assertThat(matrix.invert(inverse)).isFalse();
    matrix.setScale(1.0f, 0.0f);
    assertThat(matrix.invert(inverse)).isFalse();

    matrix.setScale(1.0f, 1.0f);
    checkInverse(matrix);
    matrix.setScale(-3.0f, 5.0f);
    checkInverse(matrix);
    matrix.setTranslate(5.0f, 2.0f);
    checkInverse(matrix);
    matrix.setScale(-3.0f, 5.0f);
    matrix.postTranslate(5.0f, 2.0f);
    checkInverse(matrix);
    matrix.setScale(-3.0f, 5.0f);
    matrix.postRotate(-30f, 1.0f, 2.0f);
    matrix.postTranslate(5.0f, 2.0f);
    checkInverse(matrix);
  }

  @Test
  public void testMapRect() {
    final Matrix matrix = new Matrix();
    matrix.postScale(2.0f, 3.0f);
    final RectF input = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
    final RectF output1 = new RectF();
    matrix.mapRect(output1, input);
    assertThat(output1).isEqualTo(new RectF(2.0f, 3.0f, 4.0f, 6.0f));

    matrix.postScale(-1.0f, -1.0f);
    final RectF output2 = new RectF();
    matrix.mapRect(output2, input);
    assertThat(output2).isEqualTo(new RectF(-4.0f, -6.0f, -2.0f, -3.0f));
  }

  @Test
  public void testMapPoints() {
    final Matrix matrix = new Matrix();
    matrix.postTranslate(-1.0f, -2.0f);
    matrix.postScale(2.0f, 3.0f);
    final float[] input = {
      0.0f, 0.0f,
      1.0f, 2.0f
    };
    final float[] output = new float[input.length];
    matrix.mapPoints(output, input);
    assertThat(output).usingExactEquality().containsExactly(-2.0f, -6.0f, 0.0f, 0.0f);
  }

  @Test
  public void testMapVectors() {
    final Matrix matrix = new Matrix();
    matrix.postTranslate(-1.0f, -2.0f);
    matrix.postScale(2.0f, 3.0f);
    final float[] input = {
      0.0f, 0.0f,
      1.0f, 2.0f
    };
    final float[] output = new float[input.length];
    matrix.mapVectors(output, input);
    assertThat(output).usingExactEquality().containsExactly(0.0f, 0.0f, 2.0f, 6.0f);
  }

  @Test
  public void legacyShadowPathAPIs_notSupported() {
    Matrix matrix = new Matrix();
    assertThrows(
        UnsupportedOperationException.class,
        () -> ((ShadowMatrix) Shadow.extract(matrix)).getDescription());
  }

  private static PointF mapPoint(Matrix matrix, float x, float y) {
    float[] pf = new float[] {x, y};
    matrix.mapPoints(pf);
    return new PointF(pf[0], pf[1]);
  }

  private static void assertPointsEqual(PointF actual, PointF expected) {
    assertThat(actual.x).isWithin(EPSILON).of(expected.x);
    assertThat(actual.y).isWithin(EPSILON).of(expected.y);
  }

  private static void checkInverse(Matrix matrix) {
    final Matrix inverse = new Matrix();
    assertThat(matrix.invert(inverse)).isTrue();
    matrix.postConcat(inverse);
    float[] vals = new float[9];
    matrix.getValues(vals);
    assertThat(vals[0]).isWithin(EPSILON).of(1);
    assertThat(vals[1]).isWithin(EPSILON).of(0);
    assertThat(vals[2]).isWithin(EPSILON).of(0);
    assertThat(vals[3]).isWithin(EPSILON).of(0);
    assertThat(vals[4]).isWithin(EPSILON).of(1);
    assertThat(vals[5]).isWithin(EPSILON).of(0);
    assertThat(vals[6]).isWithin(EPSILON).of(0);
    assertThat(vals[7]).isWithin(EPSILON).of(0);
    assertThat(vals[8]).isWithin(EPSILON).of(1);
  }
}
