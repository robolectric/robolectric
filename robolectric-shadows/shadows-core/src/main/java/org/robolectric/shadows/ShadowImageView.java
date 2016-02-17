package org.robolectric.shadows;

import android.widget.ImageView;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow for {@link android.widget.ImageView}.
 */
@Implements(ImageView.class)
public class ShadowImageView extends ShadowView {

  @RealObject
  private ImageView realImageView;

  public int getImageResourceId() {
    return ReflectionHelpers.getField(realImageView, "mResource");
  }
}
