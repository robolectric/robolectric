package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static android.os.Build.VERSION_CODES.KITKAT;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Drawable.class)
public class ShadowDrawable {
  private static int defaultIntrinsicWidth = -1;
  private static int defaultIntrinsicHeight = -1;
  static final List<String> corruptStreamSources = new ArrayList<>();

  @RealObject Drawable realDrawable;

  int createdFromResId = -1;
  InputStream createdFromInputStream;

  private int intrinsicWidth = defaultIntrinsicWidth;
  private int intrinsicHeight = defaultIntrinsicHeight;
  private int alpha;
  private boolean wasInvalidated;

  @Implementation
  public static Drawable createFromStream(InputStream is, String srcName) {
    if (corruptStreamSources.contains(srcName)) {
      return null;
    }
    BitmapDrawable drawable = new BitmapDrawable(ReflectionHelpers.callConstructor(Bitmap.class));
    ShadowBitmapDrawable shadowBitmapDrawable = Shadow.extract(drawable);
    shadowBitmapDrawable.createdFromInputStream = is;
    shadowBitmapDrawable.drawableCreateFromStreamSource = srcName;
    shadowBitmapDrawable.validate(); // start off not invalidated
    return drawable;
  }

  @Implementation // todo: this sucks, it's all just so we can detect 9-patches
  public static Drawable createFromResourceStream(Resources res, TypedValue value,
                          InputStream is, String srcName, BitmapFactory.Options opts) {
    if (is == null) {
      return null;
    }
    Rect pad = new Rect();
    if (opts == null) opts = new BitmapFactory.Options();
    opts.inScreenDensity = DisplayMetrics.DENSITY_DEFAULT;

    Bitmap  bm = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
    if (bm != null) {
      boolean isNinePatch = srcName != null && srcName.contains(".9.");
      if (isNinePatch) {
        ReflectionHelpers.callInstanceMethod(bm, "setNinePatchChunk", ClassParameter.from(byte[].class, new byte[0]));
      }
      byte[] np = bm.getNinePatchChunk();
      if (np == null || !NinePatch.isNinePatchChunk(np)) {
        np = null;
        pad = null;
      }

      if (np != null) {
        // todo: wrong
        return new NinePatchDrawable(res, bm, np, pad, srcName);
      }

      return new BitmapDrawable(res, bm);
    }
    return null;
  }

  @Implementation
  public static Drawable createFromPath(String pathName) {
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

  @Implementation
  public int getIntrinsicWidth() {
    return intrinsicWidth;
  }

  @Implementation
  public int getIntrinsicHeight() {
    return intrinsicHeight;
  }

  public static void addCorruptStreamSource(String src) {
    corruptStreamSources.add(src);
  }

  @Resetter
  public static void clearCorruptStreamSources() {
    corruptStreamSources.clear();
  }

  public static void setDefaultIntrinsicWidth(int defaultIntrinsicWidth) {
    ShadowDrawable.defaultIntrinsicWidth = defaultIntrinsicWidth;
  }

  public static void setDefaultIntrinsicHeight(int defaultIntrinsicHeight) {
    ShadowDrawable.defaultIntrinsicHeight = defaultIntrinsicHeight;
  }

  public void setIntrinsicWidth(int intrinsicWidth) {
    this.intrinsicWidth = intrinsicWidth;
  }

  public void setIntrinsicHeight(int intrinsicHeight) {
    this.intrinsicHeight = intrinsicHeight;
  }

  public InputStream getInputStream() {
    return createdFromInputStream;
  }

  @Override @Implementation
  public boolean equals(Object o) {
    if (realDrawable == o) return true;
    if (o == null || realDrawable.getClass() != o.getClass()) return false;

    ShadowDrawable that = Shadow.extract((Drawable) o);

    if (intrinsicHeight != that.intrinsicHeight) return false;
    if (intrinsicWidth != that.intrinsicWidth) return false;
    Rect bounds = realDrawable.getBounds();
    Rect thatBounds = that.realDrawable.getBounds();
    if (bounds != null ? !bounds.equals(thatBounds) : thatBounds != null) return false;

    return true;
  }

  @Override @Implementation
  public int hashCode() {
    Rect bounds = realDrawable.getBounds();
    int result = bounds != null ? bounds.hashCode() : 0;
    result = 31 * result + intrinsicWidth;
    result = 31 * result + intrinsicHeight;
    return result;
  }

  @Implementation
  public void setAlpha(int alpha) {
    this.alpha = alpha;
    Shadow.directlyOn(realDrawable, Drawable.class).setAlpha(alpha);
  }

  @Implementation
  public void invalidateSelf() {
    wasInvalidated = true;
    Shadow.directlyOn(realDrawable, Drawable.class, "invalidateSelf");
  }

  @Implementation(minSdk = KITKAT)
  public int getAlpha() {
    return alpha;
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
}
