package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its &#064;{@link LooperMode} annotation. */
@AutoService(Configurer.class)
public class LooperModeConfigurer extends SingleValueConfigurer<LooperMode, LooperMode.Mode> {

  public LooperModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        LooperMode.class, LooperMode.Mode.class, Mode.PAUSED, propertyFileLoader, systemProperties);
  }
}
