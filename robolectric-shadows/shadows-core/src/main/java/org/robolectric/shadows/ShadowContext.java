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
  public static final File CACHE_DIR = createTempDir("android-cache");
  public static final File EXTERNAL_CACHE_DIR = createTempDir("android-external-cache");
  public static final File FILES_DIR = createTempDir("android-tmp");
  public static final File EXTERNAL_FILES_DIR = createTempDir("android-external-files");
  public static final File DATABASE_DIR = createTempDir("android-database");

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
  public File getCacheDir() {
    CACHE_DIR.mkdirs();
    return CACHE_DIR;
  }

  @Implementation
  public File getFilesDir() {
    FILES_DIR.mkdirs();
    return FILES_DIR;
  }

  @Implementation
  public String[] fileList() {
    return getFilesDir().list();
  }

  @Implementation
  public File getDatabasePath(String name) {
    File file = new File(name);
    if (file.isAbsolute()) {
      return file;
    } else {
      DATABASE_DIR.mkdirs();
      return new File(DATABASE_DIR, name);
    }
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

  @Implementation
  public FileInputStream openFileInput(String path) throws FileNotFoundException {
    return new FileInputStream(getFileStreamPath(path));
  }

  @Implementation
  public FileOutputStream openFileOutput(String path, int mode) throws FileNotFoundException {
    if ((mode & Context.MODE_APPEND) == Context.MODE_APPEND) {
      return new FileOutputStream(getFileStreamPath(path), true);
    }
    return new FileOutputStream(getFileStreamPath(path));
  }

  @Implementation
  public File getFileStreamPath(String name) {
    if (name.contains(File.separator)) {
      throw new IllegalArgumentException("File " + name + " contains a path separator");
    }
    return new File(getFilesDir(), name);
  }

  @Implementation
  public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
    return openOrCreateDatabase(name, mode, factory, null);
  }

  @Implementation
  public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler databaseErrorHandler) {
    return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), factory, databaseErrorHandler);
  }

  @Implementation
  public boolean deleteFile(String name) {
    return getFileStreamPath(name).delete();
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
    clearFiles(FILES_DIR);
    clearFiles(CACHE_DIR);
    clearFiles(EXTERNAL_CACHE_DIR);
    clearFiles(EXTERNAL_FILES_DIR);
    clearFiles(DATABASE_DIR);
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
