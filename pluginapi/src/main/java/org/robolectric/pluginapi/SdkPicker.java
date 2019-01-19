package org.robolectric.pluginapi;

import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.pluginapi.ConfigurationStrategy.ConfigCollection;

public interface SdkPicker {

  @Nonnull
  List<Sdk> selectSdks(ConfigCollection configCollection, UsesSdk usesSdk);
}
