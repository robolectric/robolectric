package org.robolectric.android;

import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.ShadowMap;

public interface SandboxConfigurer {
  void configure(InstrumentationConfiguration.Builder builder);

  void configure(ShadowMap.Builder builder);
}
