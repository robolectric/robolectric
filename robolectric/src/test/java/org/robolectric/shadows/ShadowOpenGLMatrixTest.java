package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.opengl.Matrix;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowOpenGLMatrixTest {
  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfResIsNull() throws Exception {
    Matrix.multiplyMM(null, 0, new float[16], 0, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfLhsIsNull() throws Exception {
    Matrix.multiplyMM(new float[16], 0, null, 0, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfRhsIsNull() throws Exception {
    Matrix.multiplyMM(new float[16], 0, new float[16], 0, null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfResIsSmall() throws Exception {
    Matrix.multiplyMM(new float[15], 0, new float[16], 0, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfLhsIsSmall() throws Exception {
    Matrix.multiplyMM(new float[16], 0, new float[15], 0, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfRhsIsSmall() throws Exception {
    Matrix.multiplyMM(new float[16], 0, new float[16], 0, new float[15], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfResOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMM(new float[32], 30, new float[16], 0, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfLhsOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMM(new float[16], 0, new float[32], 30, new float[16], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMM_failIfRhsOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMM(new float[16], 0, new float[16], 0, new float[32], 30);
  }

  @Test
  public void multiplyIdentity() throws Exception {
    final float[] res = new float[16];
    final float[] i = new float[16];
    Matrix.setIdentityM(i, 0);
    final float[] m1 = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    Matrix.multiplyMM(res, 0, m1, 0, i, 0);
    assertThat(res).usingExactEquality().containsAllOf(m1);

    Matrix.multiplyMM(res, 0, i, 0, m1, 0);
    assertThat(res).usingExactEquality().containsAllOf(m1);
  }

  @Test
  public void multiplyIdentityWithOffset() throws Exception {
    final float[] res = new float[32];
    final float[] i = new float[32];
    Matrix.setIdentityM(i, 16);
    final float[] m1 = new float[]{
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,

            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    Matrix.multiplyMM(res, 16, m1, 16, i, 16);
    assertThat(res).usingExactEquality().containsAllOf(m1);

    Matrix.multiplyMM(res, 16, i, 16, m1, 16);
    assertThat(res).usingExactEquality().containsAllOf(m1);
  }

  @Test
  public void multiplyMM() throws Exception {
    final float[] res = new float[16];
    final float[] m1 = new float[]{
            0, 1, 2, 3,
            4, 5, 6, 7,
            8, 9, 10, 11,
            12, 13, 14, 15
    };
    final float[] m2 = new float[]{
            0, 1, 2, 3,
            4, 5, 6, 7,
            8, 9, 10, 11,
            12, 13, 14, 15
    };

    final float[] expected = new float[]{
            56, 62, 68, 74,
            152, 174, 196, 218,
            248, 286, 324, 362,
            344, 398, 452, 506
    };


    Matrix.multiplyMM(res, 0, m1, 0, m2, 0);
    assertThat(res).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void multiplyMMWithOffset() throws Exception {
    final float[] res = new float[32];
    final float[] m1 = new float[]{
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,

            0, 1, 2, 3,
            4, 5, 6, 7,
            8, 9, 10, 11,
            12, 13, 14, 15
    };
    final float[] m2 = new float[]{
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            56, 62, 68, 74,
            152, 174, 196, 218,
            248, 286, 324, 362,
            344, 398, 452, 506
    };
    final float[] expected = new float[]{
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            1680, 1940, 2200, 2460,
            4880, 5620, 6360, 7100,
            8080, 9300, 10520, 11740,
            11280, 12980, 14680, 16380
    };

    Matrix.multiplyMM(res, 16, m1, 16, m2, 16);
    assertThat(res).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void multiplyMMRandom() throws Exception {
    final float[] m1 = new float[]{
            0.730964f, 0.006556f, 0.999294f, 0.886486f,
            0.703636f, 0.865595f, 0.464857f, 0.861619f,
            0.304945f, 0.740410f, 0.059668f, 0.876067f,
            0.048256f, 0.259968f, 0.915555f, 0.356720f,
    };
    final float[] m2 = new float[]{
            0.462205f, 0.868120f, 0.520904f, 0.959729f,
            0.531887f, 0.882446f, 0.293452f, 0.878477f,
            0.938628f, 0.796945f, 0.757566f, 0.983955f,
            0.346051f, 0.972866f, 0.773706f, 0.895736f,
    };

    final float[] expected = new float[]{
            1.153855f, 1.389652f, 1.775197f, 1.956428f,
            1.141589f, 1.212979f, 1.763527f, 1.802296f,
            1.525360f, 1.512691f, 2.254498f, 2.533418f,
            1.216656f, 1.650098f, 1.664312f, 2.142354f,
    };
    final float[] res = new float[16];
    Matrix.multiplyMM(res, 0, m1, 0, m2, 0);
    assertMatrixWithPrecision(res, expected, 0.0001f);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfResIsNull() throws Exception {
    Matrix.multiplyMV(null, 0, new float[16], 0, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfLhsIsNull() throws Exception {
    Matrix.multiplyMV(new float[4], 0, null, 0, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfRhsIsNull() throws Exception {
    Matrix.multiplyMV(new float[4], 0, new float[16], 0, null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfResIsSmall() throws Exception {
    Matrix.multiplyMV(new float[3], 0, new float[16], 0, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfLhsIsSmall() throws Exception {
    Matrix.multiplyMV(new float[4], 0, new float[15], 0, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfRhsIsSmall() throws Exception {
    Matrix.multiplyMV(new float[4], 0, new float[16], 0, new float[3], 0);
  }


  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfResOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMV(new float[4], 1, new float[16], 0, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfLhsOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMV(new float[4], 0, new float[16], 1, new float[4], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multiplyMVFailsIfRhsOffsetIsOutOfBounds() throws Exception {
    Matrix.multiplyMV(new float[4], 0, new float[16], 0, new float[4], 1);
  }

  @Test
  public void multiplyMVIdentity() throws Exception {
    final float[] res = new float[4];
    final float[] i = new float[16];
    Matrix.setIdentityM(i, 0);
    float[] v1 = new float[]{1, 2, 3, 4};
    Matrix.multiplyMV(res, 0, i, 0, v1, 0);
    assertThat(res).usingExactEquality().containsAllOf(v1);
  }

  @Test
  public void multiplyMV() throws Exception {
    final float[] res = new float[4];
    final float[] m1 = new float[]{
            0, 1, 2, 3,
            4, 5, 6, 7,
            8, 9, 10, 11,
            12, 13, 14, 15
    };

    float[] v1 = new float[]{42, 239, 128, 1024};
    float[] expected = new float[]{14268, 15701, 17134, 18567};
    Matrix.multiplyMV(res, 0, m1, 0, v1, 0);
    assertThat(res).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void multiplyMVWithOffset() throws Exception {
    final float[] res = new float[5];
    final float[] m1 = new float[]{
            0, 0, 0, 0,
            0, 1, 2, 3,
            4, 5, 6, 7,
            8, 9, 10, 11,
            12, 13, 14, 15
    };

    float[] v1 = new float[]{
            0, 0,
            42, 239, 128, 1024
    };
    float[] expected = new float[]{
            0,
            14268, 15701, 17134, 18567
    };
    Matrix.multiplyMV(res, 1, m1, 4, v1, 2);
    assertThat(res).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void multiplyMVRandom() throws Exception {
    final float[] m1 = new float[]{
            0.575544f, 0.182558f, 0.097663f, 0.413832f,
            0.781248f, 0.466904f, 0.353418f, 0.790540f,
            0.074133f, 0.690470f, 0.619758f, 0.191669f,
            0.953532f, 0.018836f, 0.336544f, 0.972782f,
    };
    final float[] v2 = new float[]{
            0.573973f, 0.096736f, 0.330662f, 0.758732f,
    };
    final float[] expected = new float[]{
            1.153910f, 0.392554f, 0.550521f, 1.115460f,
    };
    final float[] res = new float[4];
    Matrix.multiplyMV(res, 0, m1, 0, v2, 0);
    assertMatrixWithPrecision(res, expected, 0.0001f);
  }

  @Test
  public void testLength() {
    assertThat(Matrix.length(3, 4, 5)).isWithin(0.001f).of(7.071f);
  }

  @Test
  public void testInvertM() {
    float[] matrix = new float[]{
            10, 0, 0, 0,
            0, 20, 0, 0,
            0, 0, 30, 0,
            40, 50, 60, 1
    };

    float[] inverse = new float[]{
            0.1f, 0, 0, 0,
            0, 0.05f, 0, 0,
            0, 0, 0.03333f, 0,
            -4, -2.5f, -2, 1
    };
    float[] output = new float[16];
    assertThat(Matrix.invertM(output, 0, matrix, 0)).isTrue();

    assertMatrixWithPrecision(output, inverse, 0.0001f);
  }

  @Test
  public void testMultiplyMM() {
    float[] matrix1 = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] matrix2 = new float[]{
            16, 15, 14, 13,
            12, 11, 10, 9,
            8, 7, 6, 5,
            4, 3, 2, 1
    };
    float[] expected = new float[]{
            386, 444, 502, 560,
            274, 316, 358, 400,
            162, 188, 214, 240,
            50, 60, 70, 80,
    };

    float[] output = new float[16];
    Matrix.multiplyMM(output, 0, matrix1, 0, matrix2, 0);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  @Config(minSdk = JELLY_BEAN, maxSdk = JELLY_BEAN)
  public void testFrustumM() {
    // this is actually a bug
    // https://android.googlesource.com/platform/frameworks/base/+/0a088f5d4681fd2da6f610de157bf905df787bf7
    // expected[8] should be 1.5
    // see testFrustumJB_MR1 below
    float[] expected = new float[]{
            0.005f, 0, 0, 0,
            0, 0.02f, 0, 0,
            3f, 5, -1.020202f, -1,
            0, 0, -2.020202f, 0,
    };
    float[] output = new float[16];
    Matrix.frustumM(output, 0, 100, 500, 200, 300, 1, 100);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testFrustumJB_MR1() {
    float[] expected = new float[]{
            0.005f, 0, 0, 0,
            0, 0.02f, 0, 0,
            1.5f, 5, -1.020202f, -1,
            0, 0, -2.020202f, 0,
    };
    float[] output = new float[16];
    Matrix.frustumM(output, 0, 100, 500, 200, 300, 1, 100);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testPerspectiveM() {
    float[] expected = new float[]{
            1145.9144f, 0, 0, 0,
            0, 572.9572f, 0, 0,
            0, 0, -1.020202f, -1,
            0, 0, -2.020202f, 0,
    };
    float[] output = new float[16];
    Matrix.perspectiveM(output, 0, 0.2f, 0.5f, 1, 100);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testMultiplyMV() {
    float[] matrix = new float[]{
            2, 0, 0, 0,
            0, 4, 0, 0,
            0, 0, 6, 0,
            1, 2, 3, 1
    };

    float[] vector = new float[]{5, 7, 9, 1};
    float[] expected = new float[]{11, 30, 57, 1};
    float[] output = new float[4];
    Matrix.multiplyMV(output, 0, matrix, 0, vector, 0);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testSetIdentityM() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };
    Matrix.setIdentityM(matrix, 0);
    assertThat(matrix).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testScaleM() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            2, 4, 6, 8,
            20, 24, 28, 32,
            54, 60, 66, 72,
            13, 14, 15, 16
    };
    float[] output = new float[16];
    Matrix.scaleM(output, 0, matrix, 0, 2, 4, 6);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testScaleMInPlace() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            2, 4, 6, 8,
            20, 24, 28, 32,
            54, 60, 66, 72,
            13, 14, 15, 16
    };
    Matrix.scaleM(matrix, 0, 2, 4, 6);
    assertThat(matrix).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testTranslateM() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            89, 102, 115, 128
    };
    float[] output = new float[16];
    Matrix.translateM(output, 0, matrix, 0, 2, 4, 6);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testTranslateMInPlace() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            89, 102, 115, 128
    };
    Matrix.translateM(matrix, 0, 2, 4, 6);
    assertThat(matrix).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testRotateM() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            0.95625275f, 1.9625025f, 2.968752f, 3.9750016f,
            5.0910234f, 6.07802f, 7.0650167f, 8.052013f,
            8.953606f, 9.960234f, 10.966862f, 11.973489f,
            13, 14, 15, 16
    };
    float[] output = new float[16];
    Matrix.rotateM(output, 0, matrix, 0, 2, 4, 6, 8);
    assertThat(output).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testRotateMInPlace() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            0.95625275f, 1.9625025f, 2.968752f, 3.9750016f,
            5.0910234f, 6.07802f, 7.0650167f, 8.052013f,
            8.953606f, 9.960234f, 10.966862f, 11.973489f,
            13, 14, 15, 16
    };
    Matrix.rotateM(matrix, 0, 2, 4, 6, 8);
    assertThat(matrix).usingExactEquality().containsAllOf(expected);
  }

  @Test
  public void testSetRotateM() {
    float[] matrix = new float[]{
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 14, 15, 16
    };
    float[] expected = new float[]{
            0.9998687f, 0.01299483f, -0.00968048f, 0,
            -0.012931813f, 0.999895f, 0.006544677f, 0,
            0.009764502f, -0.006418644f, 0.99993175f, 0,
            0, 0, 0, 1
    };
    Matrix.setRotateM(matrix, 0, 1, 2, 3, 4);
    assertThat(matrix).usingExactEquality().containsAllOf(expected);
  }

  private static void assertMatrixWithPrecision(float[] actual, float[] expected, float precision) {
    assertThat(actual).hasLength(expected.length);
    for (int i = 0; i < actual.length; i++) {
      assertThat(actual[i]).isWithin(precision).of(expected[i]);
    }
  }
}
