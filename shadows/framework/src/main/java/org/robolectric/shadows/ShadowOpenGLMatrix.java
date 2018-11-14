package org.robolectric.shadows;

import android.opengl.Matrix;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Matrix.class)
public class ShadowOpenGLMatrix {

  /**
   * Multiplies two 4x4 matrices together and stores the result in a third 4x4 matrix. In matrix
   * notation: result = lhs x rhs. Due to the way matrix multiplication works, the result matrix
   * will have the same effect as first multiplying by the rhs matrix, then multiplying by the lhs
   * matrix. This is the opposite of what you might expect.
   *
   * <p>The same float array may be passed for result, lhs, and/or rhs. However, the result element
   * values are undefined if the result elements overlap either the lhs or rhs elements.
   *
   * @param result The float array that holds the result.
   * @param resultOffset The offset into the result array where the result is stored.
   * @param lhs The float array that holds the left-hand-side matrix.
   * @param lhsOffset The offset into the lhs array where the lhs is stored
   * @param rhs The float array that holds the right-hand-side matrix.
   * @param rhsOffset The offset into the rhs array where the rhs is stored.
   * @throws IllegalArgumentException if result, lhs, or rhs are null, or if resultOffset + 16 >
   *     result.length or lhsOffset + 16 > lhs.length or rhsOffset + 16 > rhs.length.
   */
  @Implementation
  protected static void multiplyMM(
      float[] result, int resultOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset) {
    if (result == null) {
      throw new IllegalArgumentException("result == null");
    }
    if (lhs == null) {
      throw new IllegalArgumentException("lhs == null");
    }
    if (rhs == null) {
      throw new IllegalArgumentException("rhs == null");
    }
    if (resultOffset + 16 > result.length) {
      throw new IllegalArgumentException("resultOffset + 16 > result.length");
    }
    if (lhsOffset + 16 > lhs.length) {
      throw new IllegalArgumentException("lhsOffset + 16 > lhs.length");
    }
    if (rhsOffset + 16 > rhs.length) {
      throw new IllegalArgumentException("rhsOffset + 16 > rhs.length");
    }
    for (int i = 0; i < 4; i++) {
      final float rhs_i0 = rhs[I(i, 0, rhsOffset)];
      float ri0 = lhs[I(0, 0, lhsOffset)] * rhs_i0;
      float ri1 = lhs[I(0, 1, lhsOffset)] * rhs_i0;
      float ri2 = lhs[I(0, 2, lhsOffset)] * rhs_i0;
      float ri3 = lhs[I(0, 3, lhsOffset)] * rhs_i0;
      for (int j = 1; j < 4; j++) {
        final float rhs_ij = rhs[I(i, j, rhsOffset)];
        ri0 += lhs[I(j, 0, lhsOffset)] * rhs_ij;
        ri1 += lhs[I(j, 1, lhsOffset)] * rhs_ij;
        ri2 += lhs[I(j, 2, lhsOffset)] * rhs_ij;
        ri3 += lhs[I(j, 3, lhsOffset)] * rhs_ij;
      }
      result[I(i, 0, resultOffset)] = ri0;
      result[I(i, 1, resultOffset)] = ri1;
      result[I(i, 2, resultOffset)] = ri2;
      result[I(i, 3, resultOffset)] = ri3;
    }
  }

  /**
   * Multiplies a 4 element vector by a 4x4 matrix and stores the result in a 4-element column
   * vector. In matrix notation: result = lhs x rhs
   *
   * <p>The same float array may be passed for resultVec, lhsMat, and/or rhsVec. However, the
   * resultVec element values are undefined if the resultVec elements overlap either the lhsMat or
   * rhsVec elements.
   *
   * @param resultVec The float array that holds the result vector.
   * @param resultVecOffset The offset into the result array where the result vector is stored.
   * @param lhsMat The float array that holds the left-hand-side matrix.
   * @param lhsMatOffset The offset into the lhs array where the lhs is stored
   * @param rhsVec The float array that holds the right-hand-side vector.
   * @param rhsVecOffset The offset into the rhs vector where the rhs vector is stored.
   * @throws IllegalArgumentException if resultVec, lhsMat, or rhsVec are null, or if
   *     resultVecOffset + 4 > resultVec.length or lhsMatOffset + 16 > lhsMat.length or rhsVecOffset
   *     + 4 > rhsVec.length.
   */
  @Implementation
  protected static void multiplyMV(
      float[] resultVec,
      int resultVecOffset,
      float[] lhsMat,
      int lhsMatOffset,
      float[] rhsVec,
      int rhsVecOffset) {
    if (resultVec == null) {
      throw new IllegalArgumentException("resultVec == null");
    }
    if (lhsMat == null) {
      throw new IllegalArgumentException("lhsMat == null");
    }
    if (rhsVec == null) {
      throw new IllegalArgumentException("rhsVec == null");
    }
    if (resultVecOffset + 4 > resultVec.length) {
      throw new IllegalArgumentException("resultVecOffset + 4 > resultVec.length");
    }
    if (lhsMatOffset + 16 > lhsMat.length) {
      throw new IllegalArgumentException("lhsMatOffset + 16 > lhsMat.length");
    }
    if (rhsVecOffset + 4 > rhsVec.length) {
      throw new IllegalArgumentException("rhsVecOffset + 4 > rhsVec.length");
    }
    final float x = rhsVec[rhsVecOffset + 0];
    final float y = rhsVec[rhsVecOffset + 1];
    final float z = rhsVec[rhsVecOffset + 2];
    final float w = rhsVec[rhsVecOffset + 3];
    resultVec[resultVecOffset + 0] = lhsMat[I(0, 0, lhsMatOffset)] * x + lhsMat[I(1, 0, lhsMatOffset)] * y + lhsMat[I(2, 0, lhsMatOffset)] * z + lhsMat[I(3, 0, lhsMatOffset)] * w;
    resultVec[resultVecOffset + 1] = lhsMat[I(0, 1, lhsMatOffset)] * x + lhsMat[I(1, 1, lhsMatOffset)] * y + lhsMat[I(2, 1, lhsMatOffset)] * z + lhsMat[I(3, 1, lhsMatOffset)] * w;
    resultVec[resultVecOffset + 2] = lhsMat[I(0, 2, lhsMatOffset)] * x + lhsMat[I(1, 2, lhsMatOffset)] * y + lhsMat[I(2, 2, lhsMatOffset)] * z + lhsMat[I(3, 2, lhsMatOffset)] * w;
    resultVec[resultVecOffset + 3] = lhsMat[I(0, 3, lhsMatOffset)] * x + lhsMat[I(1, 3, lhsMatOffset)] * y + lhsMat[I(2, 3, lhsMatOffset)] * z + lhsMat[I(3, 3, lhsMatOffset)] * w;
  }

  private static int I(int i, int j, int offset) {
    // #define I(_i, _j) ((_j)+ 4*(_i))
    return offset + j + 4 * i;
  }

}
