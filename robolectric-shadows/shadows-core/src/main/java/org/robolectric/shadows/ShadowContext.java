package org.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.io.*;
import java.util.List;
import java.util.UUID;

import static org.robolectric.Shadows.shadowOf;

/**
 * Calls through to the {@code resourceLoader} to actually load resources.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Context.class)
abstract public class ShadowContext {
  public static final File EXTERNAL_CACHE_DIR = createTempDir("android-external-cache");
  public static final File EXTERNAL_FILES_DIR = createTempDir("android-external-files");

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
    EXTERNAL_CACHE_DIR.mkdirs();
    return EXTERNAL_CACHE_DIR;
  }

  @Implementation
  public File getExternalFilesDir(String type) {
    File f = (type == null) ? EXTERNAL_FILES_DIR : new File( EXTERNAL_FILES_DIR, type );
    f.mkdirs();
    return f;
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

  @Resetter
  public static void reset() {
    clearFiles(EXTERNAL_CACHE_DIR);
    clearFiles(EXTERNAL_FILES_DIR);
  }

  public static void clearFiles(File dir) {
    if (dir != null && dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory()) {
            clearFiles(f);
          }
          f.delete();
        }
      }
      dir.delete();
      dir.getParentFile().delete();
    }
  }

  private static File createTempDir(String name) {
    try {
      File tmp = File.createTempFile(name, "robolectric");
      if (!tmp.delete()) throw new IOException("could not delete "+tmp);
      tmp = new File(tmp, UUID.randomUUID().toString());
      if (!tmp.mkdirs()) throw new IOException("could not create "+tmp);
      tmp.deleteOnExit();

      return tmp;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
