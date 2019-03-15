package org.robolectric.shadows;

import android.graphics.RenderNode;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = RenderNode.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowRenderNodeQ {
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
  protected boolean setAlpha(float alpha) {
    this.alpha = alpha;
    return true;
  }

  @Implementation
  protected float getAlpha() {
    return alpha;
  }

  @Implementation
  protected boolean setCameraDistance(float cameraDistance) {
    this.cameraDistance = cameraDistance;
    return true;
  }

  @Implementation
  protected float getCameraDistance() {
    return cameraDistance;
  }

  @Implementation
  protected boolean setClipToOutline(boolean clipToOutline) {
    this.clipToOutline = clipToOutline;
    return true;
  }

  @Implementation
  protected boolean getClipToOutline() {
    return clipToOutline;
  }

  @Implementation
  protected boolean setElevation(float lift) {
    elevation = lift;
    return true;
  }

  @Implementation
  protected float getElevation() {
    return elevation;
  }

  @Implementation
  protected boolean setHasOverlappingRendering(boolean overlappingRendering) {
    this.overlappingRendering = overlappingRendering;
    return true;
  }

  @Implementation
  protected boolean hasOverlappingRendering() {
    return overlappingRendering;
  }

  @Implementation
  protected boolean setRotation(float rotation) {
    this.rotation = rotation;
    return true;
  }

  @Implementation
  protected float getRotation() {
    return rotation;
  }

  @Implementation
  protected boolean setRotationX(float rotationX) {
    this.rotationX = rotationX;
    return true;
  }

  @Implementation
  protected float getRotationX() {
    return rotationX;
  }

  @Implementation
  protected boolean setRotationY(float rotationY) {
    this.rotationY = rotationY;
    return true;
  }

  @Implementation
  protected float getRotationY() {
    return rotationY;
  }

  @Implementation
  protected boolean setScaleX(float scaleX) {
    this.scaleX = scaleX;
    return true;
  }

  @Implementation
  protected float getScaleX() {
    return scaleX;
  }

  @Implementation
  protected boolean setScaleY(float scaleY) {
    this.scaleY = scaleY;
    return true;
  }

  @Implementation
  protected float getScaleY() {
    return scaleY;
  }

  @Implementation
  protected boolean setTranslationX(float translationX) {
    this.translationX = translationX;
    return true;
  }

  @Implementation
  protected boolean setTranslationY(float translationY) {
    this.translationY = translationY;
    return true;
  }

  @Implementation
  protected boolean setTranslationZ(float translationZ) {
    this.translationZ = translationZ;
    return true;
  }

  @Implementation
  protected float getTranslationX() {
    return translationX;
  }

  @Implementation
  protected float getTranslationY() {
    return translationY;
  }

  @Implementation
  protected float getTranslationZ() {
    return translationZ;
  }

  @Implementation
  protected boolean isPivotExplicitlySet() {
    return pivotExplicitlySet;
  }

  @Implementation
  protected boolean setPivotX(float pivotX) {
    this.pivotX = pivotX;
    this.pivotExplicitlySet = true;
    return true;
  }

  @Implementation
  protected float getPivotX() {
    return pivotX;
  }

  @Implementation
  protected boolean setPivotY(float pivotY) {
    this.pivotY = pivotY;
    this.pivotExplicitlySet = true;
    return true;
  }

  @Implementation
  protected float getPivotY() {
    return pivotY;
  }

  @Implementation
  protected static boolean nIsValid(long n) {
    return true;
  }
}
