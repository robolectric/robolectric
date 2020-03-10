package org.robolectric.junit;

import javax.annotation.Nonnull;
import org.robolectric.android.SandboxConfigurer;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.ShadowInfo;
import org.robolectric.internal.bytecode.ShadowMap;

public class SandboxConfigurerFromConfig implements SandboxConfigurer {
  @Nonnull
  public final Config config;

  public SandboxConfigurerFromConfig(@Nonnull Config config) {
    this.config = config;
  }

  @Override
  public void configure(InstrumentationConfiguration.Builder builder) {
    builder.doNotAcquirePackage("org.junit")
        .doNotAcquirePackage("org.hamcrest");

    for (Class<?> shadowClass : config.shadows()) {
      ShadowInfo shadowInfo = ShadowMap.obtainShadowInfo(shadowClass);
      builder.addInstrumentedClass(shadowInfo.shadowedClassName);
    }

    for (String packageName : config.instrumentedPackages()) {
      builder.addInstrumentedPackage(packageName);
    }
  }

  @Override
  public void configure(ShadowMap.Builder builder) {
    builder.addShadowClasses(config.shadows());
  }
}
