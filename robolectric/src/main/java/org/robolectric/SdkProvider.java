package org.robolectric;

import java.util.Collection;
import org.robolectric.internal.SdkConfig;

public interface SdkProvider {

  SdkConfig getMaxSdkConfig();

  SdkConfig getSdkConfig(int apiLevel);

  Collection<SdkConfig> getSupportedSdkConfigs();
}
