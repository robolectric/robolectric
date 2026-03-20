package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.widget.CompoundButton;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView {
  @RealObject CompoundButton realObject;
  private int buttonDrawableId;
  private Drawable buttonDrawable;

  @Filter
  protected void setButtonDrawable(int buttonDrawableId) {
    this.buttonDrawableId = buttonDrawableId;
  }

  @Filter
  protected void setButtonDrawable(Drawable buttonDrawable) {
    this.buttonDrawable = buttonDrawable;
  }

  public int getButtonDrawableId() {
    return buttonDrawableId;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  public Drawable getButtonDrawable() {
    return buttonDrawable;
  }
}
