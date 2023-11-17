package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.annotation.TextLayoutMode.Mode;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/** Provides configuration to Robolectric for its &#064;{@link TextLayoutMode} annotation. */
@AutoService(Configurer.class)
public class TextLayoutModeConfigurer extends SingleValueConfigurer<TextLayoutMode, Mode> {

  @SuppressWarnings("SuppressWarningsDeprecated")
  public TextLayoutModeConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(TextLayoutMode.class, Mode.class, Mode.REALISTIC, propertyFileLoader, systemProperties);
  }
}
