package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.P;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "android.view.RenderNode", isInAndroidSdk = false, minSdk = LOLLIPOP, maxSdk = P)
public class ShadowViewRenderNode {
  private float alpha = 1f;
  private float cameraDistance;
  private boolean clipToOutline;
  private float elevation;
  private boolean overlappingRendering;
  private boolean pivotExplicitlySet;
  private float pivotX;
  private float pivotY;
  private float rotation;
  private float rotationX;
  private float rotationY;
  private float scaleX = 1f;
  private float scaleY = 1f;
  private float translationX;
  private float translationY;
  private float translationZ;

  @Implementation
  public boolean setAlpha(float alpha) {
    this.alpha = alpha;
    return true;
  }

  @Implementation
  public float getAlpha() {
    return alpha;
  }

  @Implementation
  public boolean setCameraDistance(float cameraDistance) {
    this.cameraDistance = cameraDistance;
    return true;
  }

  @Implementation
  public float getCameraDistance() {
    return cameraDistance;
  }

  @Implementation
  public boolean setClipToOutline(boolean clipToOutline) {
    this.clipToOutline = clipToOutline;
    return true;
  }

  @Implementation
  public boolean getClipToOutline() {
    return clipToOutline;
  }

  @Implementation
  public boolean setElevation(float lift) {
    elevation = lift;
    return true;
  }

  @Implementation
  public float getElevation() {
    return elevation;
  }

  @Implementation
  public boolean setHasOverlappingRendering(boolean overlappingRendering) {
    this.overlappingRendering = overlappingRendering;
    return true;
  }

  @Implementation
  public boolean hasOverlappingRendering() {
    return overlappingRendering;
  }

  @Implementation
  public boolean setRotation(float rotation) {
    this.rotation = rotation;
    return true;
  }

  @Implementation
  public float getRotation() {
    return rotation;
  }

  @Implementation
  public boolean setRotationX(float rotationX) {
    this.rotationX = rotationX;
    return true;
  }

  @Implementation
  public float getRotationX() {
    return rotationX;
  }

  @Implementation
  public boolean setRotationY(float rotationY) {
    this.rotationY = rotationY;
    return true;
  }

  @Implementation
  public float getRotationY() {
    return rotationY;
  }

  @Implementation
  public boolean setScaleX(float scaleX) {
    this.scaleX = scaleX;
    return true;
  }

  @Implementation
  public float getScaleX() {
    return scaleX;
  }

  @Implementation
  public boolean setScaleY(float scaleY) {
    this.scaleY = scaleY;
    return true;
  }

  @Implementation
  public float getScaleY() {
    return scaleY;
  }

  @Implementation
  public boolean setTranslationX(float translationX) {
    this.translationX = translationX;
    return true;
  }

  @Implementation
  public boolean setTranslationY(float translationY) {
    this.translationY = translationY;
    return true;
  }

  @Implementation
  public boolean setTranslationZ(float translationZ) {
    this.translationZ = translationZ;
    return true;
  }

  @Implementation
  public float getTranslationX() {
    return translationX;
  }

  @Implementation
  public float getTranslationY() {
    return translationY;
  }

  @Implementation
  public float getTranslationZ() {
    return translationZ;
  }

  @Implementation
  public boolean isPivotExplicitlySet() {
    return pivotExplicitlySet;
  }

  @Implementation
  public boolean setPivotX(float pivotX) {
    this.pivotX = pivotX;
    this.pivotExplicitlySet = true;
    return true;
  }

  @Implementation
  public float getPivotX() {
    return pivotX;
  }

  @Implementation
  public boolean setPivotY(float pivotY) {
    this.pivotY = pivotY;
    this.pivotExplicitlySet = true;
    return true;
  }

  @Implementation
  public float getPivotY() {
    return pivotY;
  }

  @Implementation
  protected boolean isValid() {
    return true;
  }
}