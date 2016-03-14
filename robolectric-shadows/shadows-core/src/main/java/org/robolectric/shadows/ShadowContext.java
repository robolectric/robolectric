package org.robolectric.shadows;

import android.content.Context;
import android.os.Environment;
import android.view.View;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboAttributeSet;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.io.File;
import java.util.List;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.content.Context}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Context.class)
abstract public class ShadowContext {
  @RealObject private Context realContext;
  private ShadowApplication shadowApplication;

  public RoboAttributeSet createAttributeSet(List<Attribute> attributes, Class<? extends View> viewClass) {
    return new RoboAttributeSet(attributes, shadowOf(realContext.getAssets()).getResourceLoader());
  }

  @Implementation
  public File getExternalCacheDir() {
    return Environment.getExternalStorageDirectory();
  }

  @Implementation
  public File getExternalFilesDir(String type) {
    return Environment.getExternalStoragePublicDirectory(type);
  }

  /**
   * Non-Android accessor.
   * Deprecated. Instead call through {@link ShadowAssetManager#getResourceLoader()};
   *
   * @return the {@code ResourceLoader} associated with this {@code Context}
   */
  @Deprecated
  public ResourceLoader getResourceLoader() {
    return shadowOf(realContext.getAssets()).getResourceLoader();
  }

  public boolean isStrictI18n() {
    return getShadowApplication().isStrictI18n();
  }

  public ResName getResName(int resourceId) {
    return shadowOf(realContext.getAssets()).getResourceLoader().getResourceIndex().getResName(resourceId);
  }

  public ShadowApplication getShadowApplication() {
    return shadowApplication;
  }

  public void callAttachBaseContext(Context context) {
    ReflectionHelpers.callInstanceMethod(realContext, "attachBaseContext", ClassParameter.from(Context.class, context));
  }
}
