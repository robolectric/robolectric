package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView {
  @RealObject CompoundButton realObject;
  private int buttonDrawableId;
  private Drawable buttonDrawable;

  @Implementation
  protected void setButtonDrawable(int buttonDrawableId) {
    this.buttonDrawableId = buttonDrawableId;
    reflector(CompoundButtonReflector.class, realObject).setButtonDrawable(buttonDrawableId);
  }

  @Implementation
  protected void setButtonDrawable(Drawable buttonDrawable) {
    this.buttonDrawable = buttonDrawable;
    reflector(CompoundButtonReflector.class, realObject).setButtonDrawable(buttonDrawable);
  }

  public int getButtonDrawableId() {
    return buttonDrawableId;
  }

  public Drawable getButtonDrawable() {
    return buttonDrawable;
  }

  @ForType(CompoundButton.class)
  interface CompoundButtonReflector {

    @Direct
    void setButtonDrawable(int buttonDrawableId);

    @Direct
    void setButtonDrawable(Drawable buttonDrawable);
  }
}
