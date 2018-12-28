package org.robolectric.internal;

import java.util.Collection;

public interface SdkProvider {

  SdkConfig getMaxSdkConfig();

  SdkConfig getSdkConfig(int apiLevel);

  Collection<SdkConfig> getSupportedSdkConfigs();
}
