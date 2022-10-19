package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Drawable.class)
public class ShadowDrawable {

  @RealObject Drawable realDrawable;

  int createdFromResId = -1;
  InputStream createdFromInputStream;

  private boolean wasInvalidated;

  /**
   * Returns an invalid Drawable with the given the resource id.
   *
   * @deprecated use {@code ContextCompat.getDrawable(context, resourceId)}
   */
  @Deprecated
  public static Drawable createFromResourceId(int resourceId) {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    BitmapDrawable drawable = new BitmapDrawable(bitmap);
    ShadowBitmapDrawable shadowBitmapDrawable = Shadow.extract(drawable);
    shadowBitmapDrawable.validate(); // start off not invalidated
    shadowBitmapDrawable.setCreatedFromResId(resourceId, null);
    return drawable;
  }

  protected void setCreatedFromResId(int createdFromResId, String resourceName) {
    this.createdFromResId = createdFromResId;
  }

  public InputStream getInputStream() {
    return createdFromInputStream;
  }

  @Implementation
  protected void invalidateSelf() {
    wasInvalidated = true;
    reflector(DrawableReflector.class, realDrawable).invalidateSelf();
  }

  public int getCreatedFromResId() {
    return createdFromResId;
  }

  public boolean wasInvalidated() {
    return wasInvalidated;
  }

  public void validate() {
    wasInvalidated = false;
  }

  @ForType(Drawable.class)
  interface DrawableReflector {

    @Direct
    void invalidateSelf();
  }
}
