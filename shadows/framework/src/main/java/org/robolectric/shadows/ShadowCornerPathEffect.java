package org.robolectric.shadows;

import android.graphics.CornerPathEffect;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CornerPathEffect.class)

/**
 * Shadow for {@link android.graphics.CornerPathEffect}.
 */
public class ShadowCornerPathEffect {
  private float radius;

  public void __constructor__(float radius) {
    this.radius = radius;
   }

  public float getRadius() {
    return radius;
  }
}
