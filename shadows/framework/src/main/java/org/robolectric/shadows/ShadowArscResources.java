package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowResources.Picker;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = Resources.class, shadowPicker = Picker.class)
public class ShadowArscResources extends ShadowResources {
  @RealObject Resources realResources;

  @Resetter
  public static void reset() {
    if (!useLegacy()) {
      ShadowResources.reset();
    }
  }

  @Implementation
  protected static Resources getSystem() {
    return ShadowResources.getSystem();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  protected Drawable loadDrawable(TypedValue value, int id) {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id));
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme)
      throws Resources.NotFoundException {
    Drawable drawable = directlyOn(realResources, Resources.class, "loadDrawable",
        ClassParameter.from(TypedValue.class, value), ClassParameter.from(int.class, id), ClassParameter.from(Resources.Theme.class, theme));
    setCreatedFromResId(realResources, id, drawable);
    return drawable;
  }

  static void setCreatedFromResId(Resources resources, int id, Drawable drawable) {
    // todo: this kinda sucks, find some better way...
    if (drawable != null && Shadow.extract(drawable) instanceof ShadowDrawable) {
      ShadowDrawable shadowDrawable = Shadow.extract(drawable);
      shadowDrawable.createdFromResId = id;
      if (drawable instanceof BitmapDrawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null  && Shadow.extract(bitmap) instanceof ShadowBitmap) {
          ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
          if (shadowBitmap.createdFromResId == -1) {
            shadowBitmap.setCreatedFromResId(id, resources.getResourceName(id));
          }
        }
      }
    }
  }
}
