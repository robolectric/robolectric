package org.robolectric.plugins;


import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.ConscryptMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its @{@link ConscryptMode} annotation. */
@AutoService(Configurer.class)
public class ConscryptModeConfigurer
    extends SingleValueConfigurer<ConscryptMode, ConscryptMode.Mode> {

  public ConscryptModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        ConscryptMode.class,
        ConscryptMode.Mode.class,
        Mode.ON,
        propertyFileLoader,
        systemProperties);
  }
}
