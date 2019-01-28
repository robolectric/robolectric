package org.robolectric.internal;

import javax.inject.Named;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.pluginapi.Sdk;

@SuppressWarnings("NewApi")
public class SdkEnvironment extends Sandbox {
  private final Sdk sdk;
  private final ParallelUniverseInterface parallelUniverse;

  public SdkEnvironment(
      ParallelUniverseInterface parallelUniverse,
      ClassLoader robolectricClassLoader,
      @Named("runtimeSdk") Sdk runtimeSdk) {
    super(robolectricClassLoader);
    this.parallelUniverse = parallelUniverse;
    sdk = runtimeSdk;
  }

  public Sdk getSdk() {
    return sdk;
  }

  public ParallelUniverseInterface getParallelUniverse() {
    return parallelUniverse;
  }
}
