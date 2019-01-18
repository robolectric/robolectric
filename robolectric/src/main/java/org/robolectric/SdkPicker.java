package org.robolectric;

import java.util.Properties;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.robolectric.plugins.DefaultSdkPicker;
import org.robolectric.plugins.SdkCollection;

/** @deprecated use {@link org.robolectric.plugins.DefaultSdkPicker} instead. */
@Deprecated
public class SdkPicker extends DefaultSdkPicker {

  @Inject
  public SdkPicker(@Nonnull SdkCollection sdkCollection, Properties systemProperties) {
    super(sdkCollection, systemProperties);
  }

}
