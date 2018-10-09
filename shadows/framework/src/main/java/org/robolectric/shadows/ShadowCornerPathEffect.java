package org.robolectric.shadows;

import android.graphics.CornerPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CornerPathEffect.class)
public class ShadowCornerPathEffect {
  private float radius;

  @Implementation
  protected void __constructor__(float radius) {
    this.radius = radius;
   }

  public float getRadius() {
    return radius;
  }
}
