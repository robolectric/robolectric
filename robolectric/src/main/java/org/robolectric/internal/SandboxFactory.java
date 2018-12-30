package org.robolectric.internal;

import org.robolectric.internal.bytecode.InstrumentationConfiguration;

public interface SandboxFactory {

  AndroidSandbox getSandbox(
      InstrumentationConfiguration instrumentationConfig,
      SdkConfig sdkConfig,
      boolean useLegacyResources);
}
