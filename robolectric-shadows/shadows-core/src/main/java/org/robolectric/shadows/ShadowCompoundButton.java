package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.internal.Shadow.*;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

/**
 * Shadow for {@link android.widget.CompoundButton}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView {
  @RealObject CompoundButton realObject;
  private int buttonDrawableId;
  private Drawable buttonDrawable;

  @Implementation
  public void setButtonDrawable(int buttonDrawableId) {
    this.buttonDrawableId = buttonDrawableId;
    directlyOn(realObject, CompoundButton.class, "setButtonDrawable", from(int.class, buttonDrawableId));
  }

  @Implementation
  public void setButtonDrawable(Drawable buttonDrawable) {
    this.buttonDrawable = buttonDrawable;
    directlyOn(realObject, CompoundButton.class, "setButtonDrawable", from(Drawable.class, buttonDrawable));
  }

  public int getButtonDrawableId() {
    return buttonDrawableId;
  }

  public Drawable getButtonDrawable() {
    return buttonDrawable;
  }
}
