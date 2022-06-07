package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.ReflectionHelpers.callStaticMethod;
import static org.robolectric.util.ReflectionHelpers.loadClass;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.RenderNode;
import android.os.Build;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/**
 * Android-Q only test for {@code RenderNode}'s shadow for both pre-Q & Q (where the latter's {@code
 * RenderNode} was moved to a public API to open access to it.
 */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public final class ShadowRenderNodeTest {

  @Test
  public void testGetTranslationX_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float translationX = renderNode.getTranslationX();

    assertThat(translationX).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetTranslationX_set_returnsSetTranslation() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(1.f);

    float translationX = renderNode.getTranslationX();

    assertThat(translationX).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetTranslationY_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float translationY = renderNode.getTranslationY();

    assertThat(translationY).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetTranslationY_set_returnsSetTranslation() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationY(1.f);

    float translationY = renderNode.getTranslationY();

    assertThat(translationY).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetRotationX_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float rotationX = renderNode.getRotationX();

    assertThat(rotationX).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetRotationX_set_returnsSetRotationX() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setRotationX(45.f);

    float rotationX = renderNode.getRotationX();

    assertThat(rotationX).isWithin(1e-3f).of(45.f);
  }

  @Test
  public void testGetRotationY_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float rotationY = renderNode.getRotationY();

    assertThat(rotationY).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetRotationY_set_returnsSetRotationY() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setRotationY(45.f);

    float rotationY = renderNode.getRotationY();

    assertThat(rotationY).isWithin(1e-3f).of(45.f);
  }

  @Test
  public void testGetRotationZ_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float rotationZ = renderNode.getRotationZ();

    assertThat(rotationZ).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetRotationZ_set_returnsSetRotationZ() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setRotationZ(45.f);

    float rotationZ = renderNode.getRotationZ();

    assertThat(rotationZ).isWithin(1e-3f).of(45.f);
  }

  @Test
  public void testGetScaleX_unset_returnsOne() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float scaleX = renderNode.getScaleX();

    assertThat(scaleX).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetScaleX_set_returnsSetScale() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setScaleX(2.f);

    float scaleX = renderNode.getScaleX();

    assertThat(scaleX).isWithin(1e-3f).of(2.f);
  }

  @Test
  public void testGetScaleY_unset_returnsOne() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float scaleY = renderNode.getScaleY();

    assertThat(scaleY).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetScaleY_set_returnsSetScale() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setScaleY(2.f);

    float scaleY = renderNode.getScaleY();

    assertThat(scaleY).isWithin(1e-3f).of(2.f);
  }

  @Test
  public void testGetPivotX_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float pivotX = renderNode.getPivotX();

    assertThat(pivotX).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetPivotX_set_returnsSetPivot() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotX(1.f);

    float pivotX = renderNode.getPivotX();

    assertThat(pivotX).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetPivotY_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float pivotY = renderNode.getPivotY();

    assertThat(pivotY).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetPivotY_set_returnsSetPivot() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotY(1.f);

    float pivotY = renderNode.getPivotY();

    assertThat(pivotY).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testIsPivotExplicitlySet_defaultNode_returnsFalse() {
    RenderNodeAccess renderNode = createRenderNode("test");

    boolean isExplicitlySet = renderNode.isPivotExplicitlySet();

    assertThat(isExplicitlySet).isFalse();
  }

  @Test
  public void testIsPivotExplicitlySet_setPivotX_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotX(1.f);

    boolean isExplicitlySet = renderNode.isPivotExplicitlySet();

    assertThat(isExplicitlySet).isTrue();
  }

  @Test
  public void testIsPivotExplicitlySet_setPivotY_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotY(1.f);

    boolean isExplicitlySet = renderNode.isPivotExplicitlySet();

    assertThat(isExplicitlySet).isTrue();
  }

  @Test
  public void testIsPivotExplicitlySet_setPivotXY_toDefaultValues_returnsFalse() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotX(0.f);
    renderNode.setPivotY(0.f);

    boolean isExplicitlySet = renderNode.isPivotExplicitlySet();

    // Setting the pivot to the center should result in the pivot not being explicitly set.
    assertThat(isExplicitlySet).isTrue();
  }

  @Test
  public void testHasIdentityMatrix_defaultNode_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");

    boolean hasIdentityMatrix = renderNode.hasIdentityMatrix();

    assertThat(hasIdentityMatrix).isTrue();
  }

  @Test
  public void testHasIdentityMatrix_updatedTranslationX_returnsFalse() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(1.f);

    boolean hasIdentityMatrix = renderNode.hasIdentityMatrix();

    assertThat(hasIdentityMatrix).isFalse();
  }

  @Test
  public void testHasIdentityMatrix_updatedRotationX_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setRotationX(90.f);

    boolean hasIdentityMatrix = renderNode.hasIdentityMatrix();

    // Rotations about the x-axis are not factored into the render node's transformation matrix.
    assertThat(hasIdentityMatrix).isTrue();
  }

  @Test
  public void testGetMatrix_defaultNode_returnsIdentityMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    assertThat(matrix.isIdentity()).isTrue();
  }

  @Test
  public void testGetMatrix_updatedTranslationX_returnsNonIdentityMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    renderNode.setTranslationX(1.f);

    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);
    assertThat(matrix.isIdentity()).isFalse();
  }

  @Test
  public void testGetMatrix_updatedTranslationX_thenY_returnsDifferentMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    renderNode.setTranslationX(1.f);
    Matrix matrix1 = new Matrix();
    renderNode.getMatrix(matrix1);

    renderNode.setTranslationY(1.f);
    Matrix matrix2 = new Matrix();
    renderNode.getMatrix(matrix2);

    assertThat(matrix1).isNotEqualTo(matrix2);
  }

  @Test
  public void testGetMatrix_updatedTranslation_returnsMatrixWithTranslation() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(2.f);
    renderNode.setTranslationY(3.f);

    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] values = new float[9];
    matrix.getValues(values);
    assertThat(values[Matrix.MTRANS_X]).isWithin(1e-3f).of(2.f);
    assertThat(values[Matrix.MTRANS_Y]).isWithin(1e-3f).of(3.f);
  }

  @Test
  public void testGetMatrix_updatedRotation_noPivot_mappedPoint_pointRotatesCorrectly() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setRotationZ(90.f);
    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] point = {2.f, 5.f};
    matrix.mapPoints(point);

    // A point rotated counterclockwise 90 degrees will now be across the y-axis and flipped.
    assertThat(point[0]).isWithin(1e-3f).of(-5.f);
    assertThat(point[1]).isWithin(1e-3f).of(2.f);
  }

  @Test
  public void testGetMatrix_updatedRotation_withPivot_mappedPoint_pointRotatesCorrectly() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setPivotX(1.f);
    renderNode.setPivotY(2.f);
    renderNode.setRotationZ(90.f);
    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] point = {2.f, 5.f};
    matrix.mapPoints(point);

    /*
     * A point rotated counterclockwise 90 degrees will now be across the y-axis and flipped.
     * However, it's further translated due to the rotation above the pivot point (1, 2). See:
     *   Rotation of v about X by D: Rt(v, X, D) = R(D) * (v - X) + X
     *     where Rm(D) is the rotation transformation counterclockwise by D degrees. The above
     *     shifts the pivot point to the origin, rotates about the origin, then shifts back.
     *   Applied: Rt((2, 5), (1, 2), 90) = R(90) * ((2, 5) - (1, 2)) + (1, 2).
     *     R(90) * (1, 3) = (-3, 1) -> (-3, 1) + (1, 2) = (-2, 3)
     */
    assertThat(point[0]).isWithin(1e-3f).of(-2.f);
    assertThat(point[1]).isWithin(1e-3f).of(3.f);
  }

  @Test
  public void testGetMatrix_updatedScale_noPivot_mappedPoint_pointScalesCorrectly() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setScaleX(1.5f);
    renderNode.setScaleY(2.f);
    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] point = {2.f, 5.f};
    matrix.mapPoints(point);

    // (2, 5) * (1.5, 2) = (3, 10)
    assertThat(point[0]).isWithin(1e-3f).of(3.f);
    assertThat(point[1]).isWithin(1e-3f).of(10.f);
  }

  @Test
  public void testGetMatrix_updatedScale_withPivot_mappedPoint_pointScalesCorrectly() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setScaleX(1.5f);
    renderNode.setScaleY(2.f);
    renderNode.setPivotX(1);
    renderNode.setPivotY(2);
    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] point = {2.f, 5.f};
    matrix.mapPoints(point);

    // See the rotation about a pivot above for the explanation for performing a linear
    // transformation about a point.
    // 1. (2, 5) - (1, 2) = (1, 3)
    // 2. (1, 3) * (1.5, 2) = (1.5, 6)
    // 3. (1.5, 6) + (1, 2) = (2.5, 8)
    assertThat(point[0]).isWithin(1e-3f).of(2.5f);
    assertThat(point[1]).isWithin(1e-3f).of(8.f);
  }

  @Test
  public void testGetMatrix_updatedScaleTranslationRotation_withPivot_mappedPoint_pointXformed() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(-6.f);
    renderNode.setTranslationY(-3.f);
    renderNode.setScaleX(1.5f);
    renderNode.setScaleY(2.f);
    renderNode.setPivotX(1);
    renderNode.setPivotY(2);
    renderNode.setRotationZ(90.f);
    Matrix matrix = new Matrix();
    renderNode.getMatrix(matrix);

    float[] point = {2.f, 5.f};
    matrix.mapPoints(point);

    // See the pivot tests above for scale & rotation to follow this math. Transformations should be
    // scale, then rotation, then translation. Both the scale and rotation share a pivot.
    // 1. (2, 5) - (1, 2) = (1, 3)
    // 2. (1, 3) * (1.5, 2) = (1.5, 6)
    // 3. rotate(1.5, 6, 90) = (-6, 1.5) <simplify pivot math because it's shared>
    // 4. (-6, 1.5) + (1, 2) = (-5, 3.5)
    // 5. (-5, 3.5) + (-6, -3) = (-11, 0.5)
    assertThat(point[0]).isWithin(1e-3f).of(-11.f);
    assertThat(point[1]).isWithin(1e-3f).of(0.5f);
  }

  @Test
  public void testGetInverseMatrix_defaultNode_returnsIdentityMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    Matrix matrix = new Matrix();
    renderNode.getInverseMatrix(matrix);

    assertThat(matrix.isIdentity()).isTrue();
  }

  @Test
  public void testGetInverseMatrix_updatedTranslationX_returnsNonIdentityMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    renderNode.setTranslationX(1.f);

    Matrix matrix = new Matrix();
    renderNode.getInverseMatrix(matrix);
    assertThat(matrix.isIdentity()).isFalse();
  }

  @Test
  public void testGetInverseMatrix_updatedTranslationX_thenY_returnsDifferentMatrix() {
    RenderNodeAccess renderNode = createRenderNode("test");

    renderNode.setTranslationX(1.f);
    Matrix matrix1 = new Matrix();
    renderNode.getInverseMatrix(matrix1);

    renderNode.setTranslationY(1.f);
    Matrix matrix2 = new Matrix();
    renderNode.getInverseMatrix(matrix2);

    assertThat(matrix1).isNotEqualTo(matrix2);
  }

  @Test
  public void testGetInverseMatrix_updatedScaleTranslationRotation_withPivot_mappedPoint_inverts() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(-6.f);
    renderNode.setTranslationY(-3.f);
    renderNode.setScaleX(1.5f);
    renderNode.setScaleY(2.f);
    renderNode.setPivotX(1);
    renderNode.setPivotY(2);
    renderNode.setRotationZ(90.f);
    Matrix inverse = new Matrix();
    renderNode.getInverseMatrix(inverse);

    float[] point = {-11f, 0.5f};
    inverse.mapPoints(point);

    // See testGetMatrix_updatedScaleTranslationRotation_withPivot_mappedPoint_pointXformed above
    // for why the point (-11, 0.5) produces (2, 5) when mapped via the inverse matrix.
    assertThat(point[0]).isWithin(1e-3f).of(2.f);
    assertThat(point[1]).isWithin(1e-3f).of(5.f);
  }

  @Test
  public void testGetMatrix_complexMatrix_multipliedByInverse_producesIdentity() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setTranslationX(-6.f);
    renderNode.setTranslationY(-3.f);
    renderNode.setScaleX(1.5f);
    renderNode.setScaleY(2.f);
    renderNode.setPivotX(1);
    renderNode.setPivotY(2);
    renderNode.setRotationZ(90.f);

    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();
    Matrix product = new Matrix();
    renderNode.getMatrix(matrix);
    renderNode.getInverseMatrix(inverse);
    product.postConcat(matrix);
    product.postConcat(inverse);

    assertThat(product.isIdentity()).isTrue();
  }

  @Test
  public void testGetAlpha_unset_returnsOne() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float alpha = renderNode.getAlpha();

    assertThat(alpha).isWithin(1e-3f).of(1.f);
  }

  @Test
  public void testGetAlpha_set_returnsSetAlpha() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setAlpha(0.5f);

    float alpha = renderNode.getAlpha();

    assertThat(alpha).isWithin(1e-3f).of(0.5f);
  }

  @Test
  public void testGetCameraDistance_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float cameraDistance = renderNode.getCameraDistance();

    assertThat(cameraDistance).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetCameraDistance_set_returnsSetCameraDistance() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setCameraDistance(2.3f);

    float cameraDistance = renderNode.getCameraDistance();

    assertThat(cameraDistance).isWithin(1e-3f).of(2.3f);
  }

  @Test
  public void testGetClipToOutline_unset_returnsFalse() {
    RenderNodeAccess renderNode = createRenderNode("test");

    boolean clipToOutline = renderNode.getClipToOutline();

    assertThat(clipToOutline).isFalse();
  }

  @Test
  public void testGetClipToOutline_set_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setClipToOutline(true);

    boolean clipToOutline = renderNode.getClipToOutline();

    assertThat(clipToOutline).isTrue();
  }

  @Test
  public void testGetElevation_unset_returnsZero() {
    RenderNodeAccess renderNode = createRenderNode("test");

    float elevation = renderNode.getElevation();

    assertThat(elevation).isWithin(1e-3f).of(0.f);
  }

  @Test
  public void testGetElevation_set_returnsSetElevation() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setElevation(2.f);

    float elevation = renderNode.getElevation();

    assertThat(elevation).isWithin(1e-3f).of(2.f);
  }

  @Test
  public void testHasOverlappingRendering_unset_returnsFalse() {
    RenderNodeAccess renderNode = createRenderNode("test");

    boolean hasOverlappingRendering = renderNode.hasOverlappingRendering();

    assertThat(hasOverlappingRendering).isFalse();
  }

  @Test
  public void testHasOverlappingRendering_set_returnsTrue() {
    RenderNodeAccess renderNode = createRenderNode("test");
    renderNode.setHasOverlappingRendering(true);

    boolean hasOverlappingRendering = renderNode.hasOverlappingRendering();

    assertThat(hasOverlappingRendering).isTrue();
  }

  private static RenderNodeAccess createRenderNode(String name) {
    if (Build.VERSION.SDK_INT < Q) {
      return new RenderNodeAccessPreQ(name);
    } else {
      return new RenderNodeAccessPostQ(name);
    }
  }

  /**
   * Provides access to a {@code RenderNode} depending on the version of Android being used. This
   * class is needed since multiple versions of {@code RenderNode} exist depending on the SDK
   * version.
   */
  private interface RenderNodeAccess {
    boolean setAlpha(float alpha);

    float getAlpha();

    boolean setCameraDistance(float cameraDistance);

    float getCameraDistance();

    boolean setClipToOutline(boolean clipToOutline);

    boolean getClipToOutline();

    boolean setElevation(float lift);

    float getElevation();

    boolean setHasOverlappingRendering(boolean overlappingRendering);

    boolean hasOverlappingRendering();

    boolean setRotationZ(float rotationZ);

    float getRotationZ();

    boolean setRotationX(float rotationX);

    float getRotationX();

    boolean setRotationY(float rotationY);

    float getRotationY();

    boolean setScaleX(float scaleX);

    float getScaleX();

    boolean setScaleY(float scaleY);

    float getScaleY();

    boolean setTranslationX(float translationX);

    boolean setTranslationY(float translationY);

    boolean setTranslationZ(float translationZ);

    float getTranslationX();

    float getTranslationY();

    float getTranslationZ();

    boolean isPivotExplicitlySet();

    boolean setPivotX(float pivotX);

    float getPivotX();

    boolean setPivotY(float pivotY);

    float getPivotY();

    boolean hasIdentityMatrix();

    void getMatrix(Matrix outMatrix);

    void getInverseMatrix(Matrix outMatrix);
  }

  /** Provides access to {@link android.view.RenderNode}. */
  private static final class RenderNodeAccessPreQ implements RenderNodeAccess {
    private final Class<?> renderNodeClass;
    private final Object renderNode;

    private RenderNodeAccessPreQ(String name) {
      renderNodeClass =
          loadClass(
              ApplicationProvider.getApplicationContext().getClass().getClassLoader(),
              "android.view.RenderNode");
      renderNode =
          callStaticMethod(
              renderNodeClass,
              "create",
              ReflectionHelpers.ClassParameter.from(String.class, name),
              ReflectionHelpers.ClassParameter.from(View.class, /* val= */ null));
    }

    @Override
    public boolean setAlpha(float alpha) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setAlpha",
          ReflectionHelpers.ClassParameter.from(float.class, alpha));
    }

    @Override
    public float getAlpha() {
      return callInstanceMethod(renderNodeClass, renderNode, "getAlpha");
    }

    @Override
    public boolean setCameraDistance(float cameraDistance) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setCameraDistance",
          ReflectionHelpers.ClassParameter.from(float.class, cameraDistance));
    }

    @Override
    public float getCameraDistance() {
      return callInstanceMethod(renderNodeClass, renderNode, "getCameraDistance");
    }

    @Override
    public boolean setClipToOutline(boolean clipToOutline) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setClipToOutline",
          ReflectionHelpers.ClassParameter.from(boolean.class, clipToOutline));
    }

    @Override
    public boolean getClipToOutline() {
      return callInstanceMethod(renderNodeClass, renderNode, "getClipToOutline");
    }

    @Override
    public boolean setElevation(float lift) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setElevation",
          ReflectionHelpers.ClassParameter.from(float.class, lift));
    }

    @Override
    public float getElevation() {
      return callInstanceMethod(renderNodeClass, renderNode, "getElevation");
    }

    @Override
    public boolean setHasOverlappingRendering(boolean overlappingRendering) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setHasOverlappingRendering",
          ReflectionHelpers.ClassParameter.from(boolean.class, overlappingRendering));
    }

    @Override
    public boolean hasOverlappingRendering() {
      return callInstanceMethod(renderNodeClass, renderNode, "hasOverlappingRendering");
    }

    @Override
    public boolean setRotationZ(float rotationZ) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setRotation",
          ReflectionHelpers.ClassParameter.from(float.class, rotationZ));
    }

    @Override
    public float getRotationZ() {
      return callInstanceMethod(renderNodeClass, renderNode, "getRotation");
    }

    @Override
    public boolean setRotationX(float rotationX) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setRotationX",
          ReflectionHelpers.ClassParameter.from(float.class, rotationX));
    }

    @Override
    public float getRotationX() {
      return callInstanceMethod(renderNodeClass, renderNode, "getRotationX");
    }

    @Override
    public boolean setRotationY(float rotationY) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setRotationY",
          ReflectionHelpers.ClassParameter.from(float.class, rotationY));
    }

    @Override
    public float getRotationY() {
      return callInstanceMethod(renderNodeClass, renderNode, "getRotationY");
    }

    @Override
    public boolean setScaleX(float scaleX) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setScaleX",
          ReflectionHelpers.ClassParameter.from(float.class, scaleX));
    }

    @Override
    public float getScaleX() {
      return callInstanceMethod(renderNodeClass, renderNode, "getScaleX");
    }

    @Override
    public boolean setScaleY(float scaleY) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setScaleY",
          ReflectionHelpers.ClassParameter.from(float.class, scaleY));
    }

    @Override
    public float getScaleY() {
      return callInstanceMethod(renderNodeClass, renderNode, "getScaleY");
    }

    @Override
    public boolean setTranslationX(float translationX) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setTranslationX",
          ReflectionHelpers.ClassParameter.from(float.class, translationX));
    }

    @Override
    public boolean setTranslationY(float translationY) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setTranslationY",
          ReflectionHelpers.ClassParameter.from(float.class, translationY));
    }

    @Override
    public boolean setTranslationZ(float translationZ) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setTranslationZ",
          ReflectionHelpers.ClassParameter.from(float.class, translationZ));
    }

    @Override
    public float getTranslationX() {
      return callInstanceMethod(renderNodeClass, renderNode, "getTranslationX");
    }

    @Override
    public float getTranslationY() {
      return callInstanceMethod(renderNodeClass, renderNode, "getTranslationY");
    }

    @Override
    public float getTranslationZ() {
      return callInstanceMethod(renderNodeClass, renderNode, "getTranslationZ");
    }

    @Override
    public boolean isPivotExplicitlySet() {
      return callInstanceMethod(renderNodeClass, renderNode, "isPivotExplicitlySet");
    }

    @Override
    public boolean setPivotX(float pivotX) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setPivotX",
          ReflectionHelpers.ClassParameter.from(float.class, pivotX));
    }

    @Override
    public float getPivotX() {
      return callInstanceMethod(renderNodeClass, renderNode, "getPivotX");
    }

    @Override
    public boolean setPivotY(float pivotY) {
      return callInstanceMethod(
          renderNodeClass,
          renderNode,
          "setPivotY",
          ReflectionHelpers.ClassParameter.from(float.class, pivotY));
    }

    @Override
    public float getPivotY() {
      return callInstanceMethod(renderNodeClass, renderNode, "getPivotY");
    }

    @Override
    public boolean hasIdentityMatrix() {
      return callInstanceMethod(renderNodeClass, renderNode, "hasIdentityMatrix");
    }

    @Override
    public void getMatrix(Matrix outMatrix) {
      callInstanceMethod(
          renderNodeClass,
          renderNode,
          "getMatrix",
          ReflectionHelpers.ClassParameter.from(Matrix.class, outMatrix));
    }

    @Override
    public void getInverseMatrix(Matrix outMatrix) {
      callInstanceMethod(
          renderNodeClass,
          renderNode,
          "getInverseMatrix",
          ReflectionHelpers.ClassParameter.from(Matrix.class, outMatrix));
    }
  }

  /** Provides access to {@link android.graphics.RenderNode}. */
  @TargetApi(Q)
  private static final class RenderNodeAccessPostQ implements RenderNodeAccess {
    private final RenderNode renderNode;

    private RenderNodeAccessPostQ(String name) {
      renderNode = new RenderNode(name);
    }

    @Override
    public boolean setAlpha(float alpha) {
      return renderNode.setAlpha(alpha);
    }

    @Override
    public float getAlpha() {
      return renderNode.getAlpha();
    }

    @Override
    public boolean setCameraDistance(float cameraDistance) {
      return renderNode.setCameraDistance(cameraDistance);
    }

    @Override
    public float getCameraDistance() {
      return renderNode.getCameraDistance();
    }

    @Override
    public boolean setClipToOutline(boolean clipToOutline) {
      return renderNode.setClipToOutline(clipToOutline);
    }

    @Override
    public boolean getClipToOutline() {
      return renderNode.getClipToOutline();
    }

    @Override
    public boolean setElevation(float lift) {
      return renderNode.setElevation(lift);
    }

    @Override
    public float getElevation() {
      return renderNode.getElevation();
    }

    @Override
    public boolean setHasOverlappingRendering(boolean overlappingRendering) {
      return renderNode.setHasOverlappingRendering(overlappingRendering);
    }

    @Override
    public boolean hasOverlappingRendering() {
      return renderNode.hasOverlappingRendering();
    }

    @Override
    public boolean setRotationZ(float rotationZ) {
      return renderNode.setRotationZ(rotationZ);
    }

    @Override
    public float getRotationZ() {
      return renderNode.getRotationZ();
    }

    @Override
    public boolean setRotationX(float rotationX) {
      return renderNode.setRotationX(rotationX);
    }

    @Override
    public float getRotationX() {
      return renderNode.getRotationX();
    }

    @Override
    public boolean setRotationY(float rotationY) {
      return renderNode.setRotationY(rotationY);
    }

    @Override
    public float getRotationY() {
      return renderNode.getRotationY();
    }

    @Override
    public boolean setScaleX(float scaleX) {
      return renderNode.setScaleX(scaleX);
    }

    @Override
    public float getScaleX() {
      return renderNode.getScaleX();
    }

    @Override
    public boolean setScaleY(float scaleY) {
      return renderNode.setScaleY(scaleY);
    }

    @Override
    public float getScaleY() {
      return renderNode.getScaleY();
    }

    @Override
    public boolean setTranslationX(float translationX) {
      return renderNode.setTranslationX(translationX);
    }

    @Override
    public boolean setTranslationY(float translationY) {
      return renderNode.setTranslationY(translationY);
    }

    @Override
    public boolean setTranslationZ(float translationZ) {
      return renderNode.setTranslationZ(translationZ);
    }

    @Override
    public float getTranslationX() {
      return renderNode.getTranslationX();
    }

    @Override
    public float getTranslationY() {
      return renderNode.getTranslationY();
    }

    @Override
    public float getTranslationZ() {
      return renderNode.getTranslationZ();
    }

    @Override
    public boolean isPivotExplicitlySet() {
      return renderNode.isPivotExplicitlySet();
    }

    @Override
    public boolean setPivotX(float pivotX) {
      return renderNode.setPivotX(pivotX);
    }

    @Override
    public float getPivotX() {
      return renderNode.getPivotX();
    }

    @Override
    public boolean setPivotY(float pivotY) {
      return renderNode.setPivotY(pivotY);
    }

    @Override
    public float getPivotY() {
      return renderNode.getPivotY();
    }

    @Override
    public boolean hasIdentityMatrix() {
      return renderNode.hasIdentityMatrix();
    }

    @Override
    public void getMatrix(Matrix outMatrix) {
      renderNode.getMatrix(outMatrix);
    }

    @Override
    public void getInverseMatrix(Matrix outMatrix) {
      renderNode.getInverseMatrix(outMatrix);
    }
  }
}
