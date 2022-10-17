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
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Drawable.class)
public class ShadowDrawable {

  @RealObject Drawable realDrawable;

  int createdFromResId = -1;
  InputStream createdFromInputStream;

  private boolean wasInvalidated;

  @Implementation
  protected static Drawable createFromStream(InputStream is, String srcName) {
    BitmapDrawable drawable = new BitmapDrawable(ReflectionHelpers.callConstructor(Bitmap.class));
    ShadowBitmapDrawable shadowBitmapDrawable = Shadow.extract(drawable);
    shadowBitmapDrawable.createdFromInputStream = is;
    shadowBitmapDrawable.drawableCreateFromStreamSource = srcName;
    shadowBitmapDrawable.validate(); // start off not invalidated
    return drawable;
  }

  @Implementation
  protected static Drawable createFromPath(String pathName) {
    BitmapDrawable drawable = new BitmapDrawable(ReflectionHelpers.callConstructor(Bitmap.class));
    ShadowBitmapDrawable shadowBitmapDrawable = Shadow.extract(drawable);
    shadowBitmapDrawable.drawableCreateFromPath = pathName;
    shadowBitmapDrawable.validate(); // start off not invalidated
    return drawable;
  }

  public static Drawable createFromResourceId(int resourceId) {
    Bitmap bitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromResId = resourceId;
    BitmapDrawable drawable = new BitmapDrawable(bitmap);
    ShadowBitmapDrawable shadowBitmapDrawable = Shadow.extract(drawable);
    shadowBitmapDrawable.validate(); // start off not invalidated
    shadowBitmapDrawable.createdFromResId = resourceId;
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
