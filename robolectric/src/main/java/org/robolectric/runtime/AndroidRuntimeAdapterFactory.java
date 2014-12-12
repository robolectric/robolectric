package org.robolectric.runtime;

import android.os.Build;

public class AndroidRuntimeAdapterFactory {
  public static AndroidRuntimeAdapter getInstance() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      return new Api19AndroidRuntimeAdapter();
    } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
      return new Api21AndroidRuntimeAdapter();
    } else {
      throw new RuntimeException("Could not find AndroidRuntimeAdapter for API level: " + Build.VERSION.SDK_INT);
    }
  }
}
