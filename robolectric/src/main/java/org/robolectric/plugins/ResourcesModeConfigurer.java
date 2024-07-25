package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.ResourcesMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its {@link ResourcesMode} annotation. */
@AutoService(Configurer.class)
public class ResourcesModeConfigurer extends SingleValueConfigurer<ResourcesMode, Mode> {

  public ResourcesModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(ResourcesMode.class, Mode.class, Mode.BINARY, propertyFileLoader, systemProperties);
  }
}
