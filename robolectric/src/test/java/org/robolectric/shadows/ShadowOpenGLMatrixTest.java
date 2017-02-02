package org.robolectric.shadows;

import android.opengl.Matrix;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Arrays;

@RunWith(TestRunners.MultiApiSelfTest.class)
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
    Assert.assertArrayEquals(m1, res, 0.0001f);

    Matrix.multiplyMM(res, 0, i, 0, m1, 0);
    Assert.assertArrayEquals(m1, res, 0.0001f);
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
    Assert.assertArrayEquals(m1, res, 0.0001f);

    Matrix.multiplyMM(res, 16, i, 16, m1, 16);
    Assert.assertArrayEquals(m1, res, 0.0001f);
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
    Assert.assertArrayEquals(expected, res, 0.0001f);
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
    Assert.assertArrayEquals(expected, res, 0.0001f);
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
    Assert.assertArrayEquals(v1, res, 0.0001f);
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
    Assert.assertArrayEquals(expected, res, 0.0001f);
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
    Assert.assertArrayEquals(expected, res, 0.0001f);
  }
}
