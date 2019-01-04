package org.robolectric.pluginapi;

import java.util.Collection;
import org.robolectric.internal.SdkConfig;

public interface SdkProvider {

  SdkConfig getMaxKnownSdkConfig();

  SdkConfig getMaxSupportedSdkConfig();

  SdkConfig getSdkConfig(int apiLevel);

  Collection<SdkConfig> getSupportedSdks();

  Collection<SdkConfig> getKnownSdks();
}
