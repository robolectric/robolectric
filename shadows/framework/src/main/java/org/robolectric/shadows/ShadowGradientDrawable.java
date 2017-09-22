package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.graphics.drawable.GradientDrawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(GradientDrawable.class)
public class ShadowGradientDrawable extends ShadowDrawable {

  @RealObject
  private GradientDrawable realGradientDrawable;

  private int color;

  @Implementation
  public void setColor(int color) {
    this.color = color;
    directlyOn(realGradientDrawable, GradientDrawable.class).setColor(color);
  }

  public int getColor() {
    return color;
  }
}
