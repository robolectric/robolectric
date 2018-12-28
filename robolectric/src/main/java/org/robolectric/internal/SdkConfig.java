package org.robolectric.internal;

import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;

public class SdkConfig implements Comparable<SdkConfig> {

  private final int apiLevel;

  private final String androidVersion;
  private final String robolectricVersion;
  private final String codeName;

  SdkConfig(int apiLevel, String androidVersion, String robolectricVersion, String codeName) {
    this.apiLevel = apiLevel;
    this.androidVersion = androidVersion;
    this.robolectricVersion = robolectricVersion;
    this.codeName = codeName;
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public String getAndroidVersion() {
    return androidVersion;
  }

  public String getAndroidCodeName() {
    return codeName;
  }

  public DependencyJar getAndroidSdkDependency() {
    return new DependencyJar("org.robolectric",
        "android-all",
        getAndroidVersion() + "-robolectric-" + robolectricVersion, null);
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
