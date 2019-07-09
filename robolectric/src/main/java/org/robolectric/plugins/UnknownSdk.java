package org.robolectric.plugins;

import java.nio.file.Path;
import java.util.Locale;
import org.robolectric.pluginapi.Sdk;

class UnknownSdk extends Sdk {

  UnknownSdk(int apiLevel) {
    super(apiLevel);
  }

  @Override
  public String getAndroidVersion() {
    throw new IllegalArgumentException(getUnsupportedMessage());
  }

  @Override
  public String getAndroidCodeName() {
    throw new IllegalArgumentException(getUnsupportedMessage());
  }

  @Override
  public Path getJarPath() {
    throw new IllegalArgumentException(getUnsupportedMessage());
  }

  @Override
  public boolean isSupported() {
    return false;
  }

  @Override
  public String getUnsupportedMessage() {
    return String.format(Locale.getDefault(), "API level %d is not available", getApiLevel());
  }

  @Override
  public boolean isKnown() {
    return false;
  }

  @Override
  public void verifySupportedSdk(String testClassName) {
    throw new IllegalArgumentException(getUnsupportedMessage());
  }
}
