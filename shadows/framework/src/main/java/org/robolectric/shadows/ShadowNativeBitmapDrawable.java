package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativeBitmapDrawable.Picker;

/** Disable the legacy ShadowBitmapDrawable as it fakes the draw logic. */
@Implements(
    value = BitmapDrawable.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeBitmapDrawable extends ShadowBitmapDrawable {
  @RealObject BitmapDrawable bitmapDrawable;

  @Override
  public int getCreatedFromResId() {
    return ((ShadowNativeBitmap) Shadow.extract(bitmapDrawable.getBitmap())).getCreatedFromResId();
  }

  @Override
  protected void setCreatedFromResId(int createdFromResId, String resourceName) {
    super.setCreatedFromResId(createdFromResId, resourceName);
    Bitmap bitmap = bitmapDrawable.getBitmap();
    if (bitmap != null && Shadow.extract(bitmap) instanceof ShadowNativeBitmap) {
      ShadowNativeBitmap shadowNativeBitmap = Shadow.extract(bitmap);
      shadowNativeBitmap.setCreatedFromResId(createdFromResId);
    }
  }

  /** Shadow picker for {@link BitmapDrawable}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowBitmapDrawable.class, ShadowNativeBitmapDrawable.class);
    }
  }
}
