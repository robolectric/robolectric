package org.robolectric;

import java.util.Properties;
import javax.inject.Inject;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.plugins.DefaultSdkPicker;

/** @deprecated use {@link org.robolectric.plugins.DefaultSdkPicker} instead. */
@Deprecated
public class SdkPicker extends DefaultSdkPicker {

  @Inject
  public SdkPicker(SdkProvider sdkProvider, Properties systemProperties) {
    super(sdkProvider, systemProperties);
  }

}
