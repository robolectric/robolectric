package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.widget.ImageView}.
 */
@Implements(ImageView.class)
public class ShadowImageView extends ShadowView {

  @RealObject
  private ImageView realImageView;

  /**
   * @deprecated Prefer shadowOf(realImageView.getDrawable()).getCreatedFromResId()
   * - this method will be removed in the future.
   */
  @Deprecated
  public int getImageResourceId() {
    return shadowOf(realImageView.getDrawable()).getCreatedFromResId();
  }

  /**
   * @deprecated Prefer ((BitmapDrawable)ImageView.getDrawable()).getBitmap() - this method will
   * be removed in the future.
   */
  @Deprecated
  public Bitmap getImageBitmap() {
    return ((BitmapDrawable)realImageView.getDrawable()).getBitmap();
  }
}
