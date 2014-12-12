package org.robolectric.shadows;

import android.view.RenderNode;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(RenderNode.class)
public class ShadowRenderNode {
  private float translationX;
  private float translationY;
  private float translationZ;

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
}
