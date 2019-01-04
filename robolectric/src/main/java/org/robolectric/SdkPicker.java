package org.robolectric;

import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;

public interface SdkPicker {

  @Nonnull
  List<SdkConfig> selectSdks(Config config, UsesSdk usesSdk);
}
