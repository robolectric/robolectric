package org.robolectric.pluginapi;

import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

public interface SdkPicker {

  @Nonnull
  List<Sdk> selectSdks(Configuration configuration, UsesSdk usesSdk);
}
