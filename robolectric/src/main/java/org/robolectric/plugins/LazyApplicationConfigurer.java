package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.util.Properties;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.config.SingleValueConfigurer;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} that reads the {@link LazyApplication} to
 * dictate whether Robolectric should lazily instantiate the Application under test (as well as the
 * test Instrumentation).
 */
@AutoService(Configurer.class)
public class LazyApplicationConfigurer extends SingleValueConfigurer<LazyApplication, LazyLoad> {

  public LazyApplicationConfigurer(
      Properties systemProperties, PackagePropertiesLoader propertyFileLoader) {
    super(
        LazyApplication.class, LazyLoad.class, LazyLoad.OFF, propertyFileLoader, systemProperties);
  }
}
