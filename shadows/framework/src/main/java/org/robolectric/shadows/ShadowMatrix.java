package org.robolectric.shadows;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Matrix.class)
public class ShadowMatrix {
  public static final String TRANSLATE = "translate";
  public static final String SCALE = "scale";
  public static final String ROTATE = "rotate";
  public static final String SINCOS = "sincos";
  public static final String SKEW = "skew";
  public static final String MATRIX = "matrix";

  private static final float EPSILON = 1e-3f;

  private final Deque<String> preOps = new ArrayDeque<>();
  private final Deque<String> postOps = new ArrayDeque<>();
  private final Map<String, String> setOps = new LinkedHashMap<>();

  private SimpleMatrix mMatrix = SimpleMatrix.IDENTITY;

  @Implementation
  public void __constructor__(Matrix src) {
    set(src);
  }

  /**
   * A list of all 'pre' operations performed on this Matrix. The last operation performed will
   * be first in the list.
   * @return A list of all 'pre' operations performed on this Matrix.
   */
  public List<String> getPreOperations() {
    return Collections.unmodifiableList(new ArrayList<>(preOps));
  }

  /**
   * A list of all 'post' operations performed on this Matrix. The last operation performed will
   * be last in the list.
   * @return A list of all 'post' operations performed on this Matrix.
   */
  public List<String> getPostOperations() {
    return Collections.unmodifiableList(new ArrayList<>(postOps));
  }

  /**
   * A map of all 'set' operations performed on this Matrix.
   * @return A map of all 'set' operations performed on this Matrix.
   */
  public Map<String, String> getSetOperations() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(setOps));
  }

  @Implementation
  public boolean isIdentity() {
    return mMatrix.equals(SimpleMatrix.IDENTITY);
  }

  @Implementation
  public boolean isAffine() {
    return mMatrix.isAffine();
  }

  @Implementation
  public boolean rectStaysRect() {
    return mMatrix.rectStaysRect();
  }

  @Implementation
  public void getValues(float[] values) {
    mMatrix.getValues(values);
  }

  @Implementation
  public void setValues(float[] values) {
    mMatrix = new SimpleMatrix(values);
  }

  @Implementation
  public void set(Matrix src) {
    reset();
    if (src != null) {
      ShadowMatrix shadowMatrix = Shadows.shadowOf(src);
      preOps.addAll(shadowMatrix.preOps);
      postOps.addAll(shadowMatrix.postOps);
      setOps.putAll(shadowMatrix.setOps);
      mMatrix = new SimpleMatrix(getSimpleMatrix(src));
    }
  }

  @Implementation
  public void reset() {
    preOps.clear();
    postOps.clear();
    setOps.clear();
    mMatrix = SimpleMatrix.IDENTITY;
  }

  @Implementation
  public void setTranslate(float dx, float dy) {
    setOps.put(TRANSLATE, dx + " " + dy);
    mMatrix = SimpleMatrix.translate(dx, dy);
  }

  @Implementation
  public void setScale(float sx, float sy, float px, float py) {
    setOps.put(SCALE, sx + " " + sy + " " + px + " " + py);
    mMatrix = SimpleMatrix.scale(sx, sy, px, py);
  }

  @Implementation
  public void setScale(float sx, float sy) {
    setOps.put(SCALE, sx + " " + sy);
    mMatrix = SimpleMatrix.scale(sx, sy);
  }

  @Implementation
  public void setRotate(float degrees, float px, float py) {
    setOps.put(ROTATE, degrees + " " + px + " " + py);
    mMatrix = SimpleMatrix.rotate(degrees, px, py);
  }

  @Implementation
  public void setRotate(float degrees) {
    setOps.put(ROTATE, Float.toString(degrees));
    mMatrix = SimpleMatrix.rotate(degrees);
  }

  @Implementation
  public void setSinCos(float sinValue, float cosValue, float px, float py) {
    setOps.put(SINCOS, sinValue + " " + cosValue + " " + px + " " + py);
    mMatrix = SimpleMatrix.sinCos(sinValue, cosValue, px, py);
  }

  @Implementation
  public void setSinCos(float sinValue, float cosValue) {
    setOps.put(SINCOS, sinValue + " " + cosValue);
    mMatrix = SimpleMatrix.sinCos(sinValue, cosValue);
  }

  @Implementation
  public void setSkew(float kx, float ky, float px, float py) {
    setOps.put(SKEW, kx + " " + ky + " " + px + " " + py);
    mMatrix = SimpleMatrix.skew(kx, ky, px, py);
  }

  @Implementation
  public void setSkew(float kx, float ky) {
    setOps.put(SKEW, kx + " " + ky);
    mMatrix = SimpleMatrix.skew(kx, ky);
  }

  @Implementation
  public boolean setConcat(Matrix a, Matrix b) {
    mMatrix = getSimpleMatrix(a).multiply(getSimpleMatrix(b));
    return true;
  }

  @Implementation
  public boolean preTranslate(float dx, float dy) {
    preOps.addFirst(TRANSLATE + " " + dx + " " + dy);
    return preConcat(SimpleMatrix.translate(dx, dy));
  }

  @Implementation
  public boolean preScale(float sx, float sy, float px, float py) {
    preOps.addFirst(SCALE + " " + sx + " " + sy + " " + px + " " + py);
    return preConcat(SimpleMatrix.scale(sx, sy, px, py));
  }

  @Implementation
  public boolean preScale(float sx, float sy) {
    preOps.addFirst(SCALE + " " + sx + " " + sy);
    return preConcat(SimpleMatrix.scale(sx, sy));
  }

  @Implementation
  public boolean preRotate(float degrees, float px, float py) {
    preOps.addFirst(ROTATE + " " + degrees + " " + px + " " + py);
    return preConcat(SimpleMatrix.rotate(degrees, px, py));
  }

  @Implementation
  public boolean preRotate(float degrees) {
    preOps.addFirst(ROTATE + " " + Float.toString(degrees));
    return preConcat(SimpleMatrix.rotate(degrees));
  }

  @Implementation
  public boolean preSkew(float kx, float ky, float px, float py) {
    preOps.addFirst(SKEW + " " + kx + " " + ky + " " + px + " " + py);
    return preConcat(SimpleMatrix.skew(kx, ky, px, py));
  }

  @Implementation
  public boolean preSkew(float kx, float ky) {
    preOps.addFirst(SKEW + " " + kx + " " + ky);
    return preConcat(SimpleMatrix.skew(kx, ky));
  }

  @Implementation
  public boolean preConcat(Matrix other) {
    preOps.addFirst(MATRIX + " " + other);
    return preConcat(getSimpleMatrix(other));
  }

  @Implementation
  public boolean postTranslate(float dx, float dy) {
    postOps.addLast(TRANSLATE + " " + dx + " " + dy);
    return postConcat(SimpleMatrix.translate(dx, dy));
  }

  @Implementation
  public boolean postScale(float sx, float sy, float px, float py) {
    postOps.addLast(SCALE + " " + sx + " " + sy + " " + px + " " + py);
    return postConcat(SimpleMatrix.scale(sx, sy, px, py));
  }

  @Implementation
  public boolean postScale(float sx, float sy) {
    postOps.addLast(SCALE + " " + sx + " " + sy);
    return postConcat(SimpleMatrix.scale(sx, sy));
  }

  @Implementation
  public boolean postRotate(float degrees, float px, float py) {
    postOps.addLast(ROTATE + " " + degrees + " " + px + " " + py);
    return postConcat(SimpleMatrix.rotate(degrees, px, py));
  }

  @Implementation
  public boolean postRotate(float degrees) {
    postOps.addLast(ROTATE + " " + Float.toString(degrees));
    return postConcat(SimpleMatrix.rotate(degrees));
  }

  @Implementation
  public boolean postSkew(float kx, float ky, float px, float py) {
    postOps.addLast(SKEW + " " + kx + " " + ky + " " + px + " " + py);
    return postConcat(SimpleMatrix.skew(kx, ky, px, py));
  }

  @Implementation
  public boolean postSkew(float kx, float ky) {
    postOps.addLast(SKEW + " " + kx + " " + ky);
    return postConcat(SimpleMatrix.skew(kx, ky));
  }

  @Implementation
  public boolean postConcat(Matrix other) {
    postOps.addLast(MATRIX + " " + other);
    return postConcat(getSimpleMatrix(other));
  }

  @Implementation
  public boolean invert(Matrix inverse) {
    final SimpleMatrix inverseMatrix = mMatrix.invert();
    if (inverseMatrix != null) {
      if (inverse != null) {
        final ShadowMatrix shadowInverse = Shadow.extract(inverse);
        shadowInverse.mMatrix = inverseMatrix;
      }
      return true;
    }
    return false;
  }

  public PointF mapPoint(float x, float y) {
    return mMatrix.transform(new PointF(x, y));
  }

  public PointF mapPoint(PointF point) {
    return mMatrix.transform(point);
  }

  @Implementation
  public boolean mapRect(RectF destination, RectF source) {
    final PointF leftTop = mapPoint(source.left, source.top);
    final PointF rightBottom = mapPoint(source.right, source.bottom);
    destination.set(
        Math.min(leftTop.x, rightBottom.x),
        Math.min(leftTop.y, rightBottom.y),
        Math.max(leftTop.x, rightBottom.x),
        Math.max(leftTop.y, rightBottom.y));
    return true;
  }

  @Implementation
  @Override
  public boolean equals(Object obj) {
    final float[] values;
    if (obj instanceof Matrix) {
        return getSimpleMatrix(((Matrix) obj)).equals(mMatrix);
    } else {
        return obj instanceof ShadowMatrix && obj.equals(mMatrix);
    }
  }

  @Implementation
  @Override
  public int hashCode() {
      return Objects.hashCode(mMatrix);
  }

  public String getDescription() {
    return "Matrix[pre=" + preOps + ", set=" + setOps + ", post=" + postOps + "]";
  }

  private static SimpleMatrix getSimpleMatrix(Matrix matrix) {
    final ShadowMatrix otherMatrix = Shadow.extract(matrix);
    return otherMatrix.mMatrix;
  }

  private boolean postConcat(SimpleMatrix matrix) {
    mMatrix = matrix.multiply(mMatrix);
    return true;
  }

  private boolean preConcat(SimpleMatrix matrix) {
    mMatrix = mMatrix.multiply(matrix);
    return true;
  }

  /**
   * A simple implementation of an immutable matrix.
   */
  private static class SimpleMatrix {
    private static final SimpleMatrix IDENTITY = new SimpleMatrix(new float[] {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
    });

    private final float[] mValues;

    SimpleMatrix(SimpleMatrix matrix) {
      mValues = Arrays.copyOf(matrix.mValues, matrix.mValues.length);
    }

    private SimpleMatrix(float[] values) {
      if (values.length != 9) {
        throw new ArrayIndexOutOfBoundsException();
      }
      mValues = Arrays.copyOf(values, 9);
    }

    public boolean isAffine() {
      return mValues[6] == 0.0f && mValues[7] == 0.0f && mValues[8] == 1.0f;
    }

    public boolean rectStaysRect() {
      final float m00 = mValues[0];
      final float m01 = mValues[1];
      final float m10 = mValues[3];
      final float m11 = mValues[4];
      return (m00 == 0 && m11 == 0 && m01 != 0 && m10 != 0) || (m00 != 0 && m11 != 0 && m01 == 0 && m10 == 0);
    }

    public void getValues(float[] values) {
      if (values.length < 9) {
        throw new ArrayIndexOutOfBoundsException();
      }
      System.arraycopy(mValues, 0, values, 0, 9);
    }

    public static SimpleMatrix translate(float dx, float dy) {
      return new SimpleMatrix(new float[] {
          1.0f, 0.0f, dx,
          0.0f, 1.0f, dy,
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix scale(float sx, float sy, float px, float py) {
      return new SimpleMatrix(new float[] {
          sx,   0.0f, px * (1 - sx),
          0.0f, sy,   py * (1 - sy),
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix scale(float sx, float sy) {
      return new SimpleMatrix(new float[] {
          sx,   0.0f, 0.0f,
          0.0f, sy,   0.0f,
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix rotate(float degrees, float px, float py) {
      final double radians = Math.toRadians(degrees);
      final float sin = (float) Math.sin(radians);
      final float cos = (float) Math.cos(radians);
      return sinCos(sin, cos, px, py);
    }

    public static SimpleMatrix rotate(float degrees) {
      final double radians = Math.toRadians(degrees);
      final float sin = (float) Math.sin(radians);
      final float cos = (float) Math.cos(radians);
      return sinCos(sin, cos);
    }

    public static SimpleMatrix sinCos(float sin, float cos, float px, float py) {
      return new SimpleMatrix(new float[] {
          cos,  -sin, sin * py + (1 - cos) * px,
          sin,  cos,  -sin * px + (1 - cos) * py,
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix sinCos(float sin, float cos) {
      return new SimpleMatrix(new float[] {
          cos,  -sin, 0.0f,
          sin,  cos,  0.0f,
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix skew(float kx, float ky, float px, float py) {
      return new SimpleMatrix(new float[] {
          1.0f, kx,   -kx * py,
          ky,   1.0f, -ky * px,
          0.0f, 0.0f, 1.0f,
      });
    }

    public static SimpleMatrix skew(float kx, float ky) {
      return new SimpleMatrix(new float[] {
          1.0f, kx,   0.0f,
          ky,   1.0f, 0.0f,
          0.0f, 0.0f, 1.0f,
      });
    }

    public SimpleMatrix multiply(SimpleMatrix matrix) {
      final float[] values = new float[9];
      for (int i = 0; i < values.length; ++i) {
        final int row = i / 3;
        final int col = i % 3;
        for (int j = 0; j < 3; ++j) {
          values[i] += mValues[row * 3 + j] * matrix.mValues[j * 3 + col];
        }
      }
      return new SimpleMatrix(values);
    }

    public SimpleMatrix invert() {
      final float invDet = inverseDeterminant();
      if (invDet == 0) {
        return null;
      }

      final float[] src = mValues;
      final float[] dst = new float[9];
      dst[0] = cross_scale(src[4], src[8], src[5], src[7], invDet);
      dst[1] = cross_scale(src[2], src[7], src[1], src[8], invDet);
      dst[2] = cross_scale(src[1], src[5], src[2], src[4], invDet);

      dst[3] = cross_scale(src[5], src[6], src[3], src[8], invDet);
      dst[4] = cross_scale(src[0], src[8], src[2], src[6], invDet);
      dst[5] = cross_scale(src[2], src[3], src[0], src[5], invDet);

      dst[6] = cross_scale(src[3], src[7], src[4], src[6], invDet);
      dst[7] = cross_scale(src[1], src[6], src[0], src[7], invDet);
      dst[8] = cross_scale(src[0], src[4], src[1], src[3], invDet);
      return new SimpleMatrix(dst);
    }

    public PointF transform(PointF point) {
      return new PointF(
          point.x * mValues[0] + point.y * mValues[1] + mValues[2],
          point.x * mValues[3] + point.y * mValues[4] + mValues[5]);
    }

    @Override
    public boolean equals(Object o) {
      return this == o || (o instanceof SimpleMatrix && equals((SimpleMatrix) o));
    }

    public boolean equals(SimpleMatrix matrix) {
      if (matrix == null) {
        return false;
      }
      for (int i = 0; i < mValues.length; i++) {
        if (!isNearlyZero(matrix.mValues[i] - mValues[i])) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(mValues);
    }

    private static boolean isNearlyZero(float value) {
      return Math.abs(value) < EPSILON;
    }

    private static float cross(float a, float b, float c, float d) {
      return a * b - c * d;
    }

    private static float cross_scale(float a, float b, float c, float d, float scale) {
      return cross(a, b, c, d) * scale;
    }

    private float inverseDeterminant() {
      final float determinant = mValues[0] * cross(mValues[4], mValues[8], mValues[5], mValues[7]) +
          mValues[1] * cross(mValues[5], mValues[6], mValues[3], mValues[8]) +
          mValues[2] * cross(mValues[3], mValues[7], mValues[4], mValues[6]);
      return isNearlyZero(determinant) ? 0.0f : 1.0f / determinant;
    }
  }
}
