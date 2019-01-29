package org.robolectric.internal;

import javax.inject.Named;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.pluginapi.Sdk;

@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {
  private final Sdk sdk;
  private final Environment environment;

  public AndroidSandbox(Environment environment, ClassLoader robolectricClassLoader,
      @Named("runtimeSdk") Sdk runtimeSdk) {
    super(robolectricClassLoader);
    this.environment = environment;
    sdk = runtimeSdk;
  }

  public Sdk getSdk() {
    return sdk;
  }

  public Environment getEnvironment() {
    return environment;
  }
}
