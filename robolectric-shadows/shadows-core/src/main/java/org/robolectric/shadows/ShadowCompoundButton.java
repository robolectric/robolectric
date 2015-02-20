package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView {
  @RealObject CompoundButton realCompoundButton;
  private int buttonDrawableId;
  private Drawable buttonDrawable;

  @Implementation
  public void setButtonDrawable(int buttonDrawableId) {
    this.buttonDrawableId = buttonDrawableId;
  }

  @Implementation
  public void setButtonDrawable(Drawable buttonDrawable) {
    this.buttonDrawable = buttonDrawable;
  }

  public int getButtonDrawableId() {
    return buttonDrawableId;
  }

  public Drawable getButtonDrawable() {
    return buttonDrawable;
  }
}
