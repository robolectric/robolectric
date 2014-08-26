package org.robolectric.shadows;

import android.os.Environment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

@Implements(Environment.class)
public class ShadowEnvironment {

  private static final String MEDIA_REMOVED = "removed";

  private static String externalStorageState = MEDIA_REMOVED;

  @Implementation
  public static String getExternalStorageState() {
    return externalStorageState;
  }

  public static void setExternalStorageState(String externalStorageState) {
    ShadowEnvironment.externalStorageState = externalStorageState;
  }

  @Implementation
  public static File getExternalStorageDirectory() {
    ShadowContext.EXTERNAL_CACHE_DIR.mkdirs();
    return ShadowContext.EXTERNAL_CACHE_DIR;
  }

  @Implementation
  public static File getExternalStoragePublicDirectory(String type) {
    File f = (type == null) ? ShadowContext.EXTERNAL_FILES_DIR : new File( ShadowContext.EXTERNAL_FILES_DIR, type );
    f.mkdirs();
    return f;
  }
}
