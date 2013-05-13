package org.robolectric.shadows;

import android.graphics.Matrix;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Matrix.class)
public class ShadowMatrix {
  public static final String TRANSLATE = "translate";
  public static final String SCALE = "scale";
  public static final String ROTATE = "rotate";
  public static final String SINCOS = "sincos";
  public static final String SKEW = "skew";
  public static final String MATRIX = "matrix";

  private final Deque<String> preOps = new ArrayDeque<String>();
  private final Deque<String> postOps = new ArrayDeque<String>();
  private final Map<String, String> setOps = new LinkedHashMap<String, String>();

  public void __constructor__(Matrix src) {
    set(src);
  }

  /**
   * A list of all 'pre' operations performed on this Matrix. The last operation performed will
   * be first in the list.
   */
  public List<String> getPreOperations() {
    return Collections.unmodifiableList(new ArrayList<String>(preOps));
  }

  /**
   * A list of all 'post' operations performed on this Matrix. The last operation performed will
   * be last in the list.
   */
  public List<String> getPostOperations() {
    return Collections.unmodifiableList(new ArrayList<String>(postOps));
  }

  /** A map of all 'set' operations performed on this Matrix. */
  public Map<String, String> getSetOperations() {
    return Collections.unmodifiableMap(new LinkedHashMap<String, String>(setOps));
  }

  @Implementation
  public boolean isIdentity() {
    return preOps.isEmpty() && postOps.isEmpty() && setOps.isEmpty();
  }

  @Implementation
  public void set(Matrix src) {
    reset();

    ShadowMatrix shadowMatrix = shadowOf(src);
    preOps.addAll(shadowMatrix.preOps);
    postOps.addAll(shadowMatrix.postOps);
    setOps.putAll(shadowMatrix.setOps);
  }

  @Implementation
  public void reset() {
    preOps.clear();
    postOps.clear();
    setOps.clear();
  }

  @Implementation
  public void setTranslate(float dx, float dy) {
    setOps.put(TRANSLATE, dx + " " + dy);
  }

  @Implementation
  public void setScale(float sx, float sy, float px, float py) {
    setOps.put(SCALE, sx + " " + sy + " " + py + " " + py);
  }

  @Implementation
  public void setScale(float sx, float sy) {
    setOps.put(SCALE, sx + " " + sy);
  }

  @Implementation
  public void setRotate(float degrees, float px, float py) {
    setOps.put(ROTATE, degrees + " " + px + " " + py);
  }

  @Implementation
  public void setRotate(float degrees) {
    setOps.put(ROTATE, Float.toString(degrees));
  }

  @Implementation
  public void setSinCos(float sinValue, float cosValue, float px, float py) {
    setOps.put(SINCOS, sinValue + " " + cosValue + " " + px + " " + py);
  }

  @Implementation
  public void setSinCos(float sinValue, float cosValue) {
    setOps.put(SINCOS, sinValue + " " + cosValue);
  }

  @Implementation
  public void setSkew(float kx, float ky, float px, float py) {
    setOps.put(SKEW, kx + " " + ky + " " + px + " " + py);
  }

  @Implementation
  public void setSkew(float kx, float ky) {
    setOps.put(SKEW, kx + " " + ky);
  }

  @Implementation
  public void preTranslate(float dx, float dy) {
    preOps.addFirst(TRANSLATE + " " + dx + " " + dy);
  }

  @Implementation
  public void preScale(float sx, float sy, float px, float py) {
    preOps.addFirst(SCALE + " " + sx + " " + sy + " " + px + " " + py);
  }

  @Implementation
  public void preScale(float sx, float sy) {
    preOps.addFirst(SCALE + " " + sx + " " + sy);
  }

  @Implementation
  public void preRotate(float degrees, float px, float py) {
    preOps.addFirst(ROTATE + " " + degrees + " " + px + " " + py);
  }

  @Implementation
  public void preRotate(float degrees) {
    preOps.addFirst(ROTATE + " " + Float.toString(degrees));
  }

  @Implementation
  public void preSkew(float kx, float ky, float px, float py) {
    preOps.addFirst(SKEW + " " + kx + " " + ky + " " + px + " " + py);
  }

  @Implementation
  public void preSkew(float kx, float ky) {
    preOps.addFirst(SKEW + " " + kx + " " + ky);
  }

  @Implementation
  public void preConcat(Matrix other) {
    preOps.addFirst(MATRIX + " " + other);
  }

  @Implementation
  public void postTranslate(float dx, float dy) {
    postOps.addLast(TRANSLATE + " " + dx + " " + dy);
  }

  @Implementation
  public void postScale(float sx, float sy, float px, float py) {
    postOps.addLast(SCALE + " " + sx + " " + sy + " " + px + " " + py);
  }

  @Implementation
  public void postScale(float sx, float sy) {
    postOps.addLast(SCALE + " " + sx + " " + sy);
  }

  @Implementation
  public void postRotate(float degrees, float px, float py) {
    postOps.addLast(ROTATE + " " + degrees + " " + px + " " + py);
  }

  @Implementation
  public void postRotate(float degrees) {
    postOps.addLast(ROTATE + " " + Float.toString(degrees));
  }

  @Implementation
  public void postSkew(float kx, float ky, float px, float py) {
    postOps.addLast(SKEW + " " + kx + " " + ky + " " + px + " " + py);
  }

  @Implementation
  public void postSkew(float kx, float ky) {
    postOps.addLast(SKEW + " " + kx + " " + ky);
  }

  @Implementation
  public void postConcat(Matrix other) {
    postOps.addLast(MATRIX + " " + other);
  }

  @Implementation
  public String toString() {
    return "Matrix[pre=" + preOps + ", set=" + setOps + ", post=" + postOps + "]";
  }
}
