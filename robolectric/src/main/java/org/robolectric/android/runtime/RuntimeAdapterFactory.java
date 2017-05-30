package org.robolectric.android.runtime;

import android.os.Build;

import org.robolectric.RuntimeEnvironment;

public class RuntimeAdapterFactory {
  public static RuntimeAdapter getInstance() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel <= Build.VERSION_CODES.JELLY_BEAN) {
      return new Api16RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return new Api17RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.KITKAT) {
      return new Api19RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP) {
      return new Api21RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      return new Api22RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.M) {
      return new Api23RuntimeAdapter();
    } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
      return new Api24RuntimeAdapter();
    } else if (apiLevel == 26 || apiLevel == 10000) {
      return new Api26RuntimeAdapter();
    } else {
      throw new RuntimeException("Could not find AndroidRuntimeAdapter for API level: " + apiLevel);
    }
  }
}
