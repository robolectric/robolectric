package org.robolectric.pluginapi;

import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Sdk;

public interface SdkPicker {

  @Nonnull
  List<Sdk> selectSdks(Config config, UsesSdk usesSdk);
}
