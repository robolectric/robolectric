package org.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.view.View;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.io.*;
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

  @Implementation
  public String getString(int resId) {
    return realContext.getResources().getString(resId);
  }

  @Implementation
  public CharSequence getText(int resId) {
    return realContext.getResources().getText(resId);
  }

  @Implementation
  public String getString(int resId, Object... formatArgs) {
    return realContext.getResources().getString(resId, formatArgs);
  }

  public RoboAttributeSet createAttributeSet(List<Attribute> attributes, Class<? extends View> viewClass) {
    return new RoboAttributeSet(attributes, getResourceLoader());
  }

  @Implementation
  public Resources getResources() {
    throw new RuntimeException("you should override me in a subclass!");
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
   *
   * @return the {@code ResourceLoader} associated with this {@code Context}
   */
  public ResourceLoader getResourceLoader() {
    return shadowOf((Application) realContext.getApplicationContext()).getResourceLoader();
  }

  public boolean isStrictI18n() {
    return getShadowApplication().isStrictI18n();
  }

  public ResName getResName(int resourceId) {
    return getResourceLoader().getResourceIndex().getResName(resourceId);
  }

  public ShadowApplication getShadowApplication() {
    return shadowApplication;
  }

  public void callAttachBaseContext(Context context) {
    ReflectionHelpers.callInstanceMethod(realContext, "attachBaseContext", ClassParameter.from(Context.class, context));
  }
}
