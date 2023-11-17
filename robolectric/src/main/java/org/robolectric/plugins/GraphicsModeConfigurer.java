package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its @{@link GraphicsMode} annotation. */
@AutoService(Configurer.class)
public class GraphicsModeConfigurer extends SingleValueConfigurer<GraphicsMode, Mode> {

  public GraphicsModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        GraphicsMode.class,
        GraphicsMode.Mode.class,
        Mode.LEGACY,
        propertyFileLoader,
        systemProperties);
  }
}
