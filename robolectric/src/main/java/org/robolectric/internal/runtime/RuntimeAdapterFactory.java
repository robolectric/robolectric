package org.robolectric.internal.runtime;

import android.os.Build;

public class RuntimeAdapterFactory {
  public static RuntimeAdapter getInstance() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      return new Api19RuntimeAdapter();
    } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
      return new Api21RuntimeAdapter();
    } else {
      throw new RuntimeException("Could not find AndroidRuntimeAdapter for API level: " + Build.VERSION.SDK_INT);
    }
  }
}
