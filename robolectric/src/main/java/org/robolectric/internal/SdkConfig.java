package org.robolectric.internal;

import android.os.Build;

import java.util.*;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.util.Logger;

public class SdkConfig implements Comparable<SdkConfig> {

  private static final Set<Integer> SUPPORTED_APIS = Collections.unmodifiableSet(new HashSet<Integer>() {
    {
      add(Build.VERSION_CODES.JELLY_BEAN);
      add(Build.VERSION_CODES.JELLY_BEAN_MR1);
      add(Build.VERSION_CODES.JELLY_BEAN_MR2);
      add(Build.VERSION_CODES.KITKAT);
      add(Build.VERSION_CODES.LOLLIPOP);
      add(Build.VERSION_CODES.LOLLIPOP_MR1);
      add(Build.VERSION_CODES.M);
      add(Build.VERSION_CODES.N);
      add(Build.VERSION_CODES.N_MR1);
      add(Build.VERSION_CODES.O);
    }
  });

  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;
  public static final int MAX_SDK_VERSION = Collections.max(getSupportedApis());

  private final int apiLevel;

  public static Set<Integer> getSupportedApis() {
    return SUPPORTED_APIS;
  }

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
  }

  public int getApiLevel() {
    return apiLevel;
  }

  @Override
  public boolean equals(Object that) {
    return that == this || (that instanceof SdkConfig && ((SdkConfig) that).apiLevel == (apiLevel));
  }

  @Override
  public int hashCode() {
    return apiLevel;
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int compareTo(@Nonnull SdkConfig o) {
    return apiLevel - o.apiLevel;
  }

}
