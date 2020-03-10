package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import javax.annotation.Priority;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.GlobalConfigProvider;

@AutoService(GlobalConfigProvider.class)
@Priority(Integer.MIN_VALUE)
public class DefaultGlobalConfigProvider implements GlobalConfigProvider {
  static Config globalConfig = new Config.Builder().build();

  @Override
  public Config get() {
    return globalConfig;
  }
}
