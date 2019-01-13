package org.robolectric.plugins;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.robolectric.internal.Sdk;

class UnknownSdk implements Sdk {

  private final int apiLevel;

  UnknownSdk(int apiLevel) {
    this.apiLevel = apiLevel;
  }

  @Override
  public int getApiLevel() {
    return apiLevel;
  }

  @Override
  public String getAndroidVersion() {
    return null;
  }

  @Override
  public String getAndroidCodeName() {
    return null;
  }

  @Override
  public Path getJarPath() {
    throw new IllegalArgumentException(
        String.format("Robolectric does not support API level %d.", getApiLevel()));
  }

  @Override
  public boolean isKnown() {
    return false;
  }

  @Override
  public boolean isSupported() {
    return false;
  }

  @Override
  public int compareTo(@Nonnull Sdk o) {
    return 0;
  }
}
