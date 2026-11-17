package org.robolectric.junit.platform.internal;

import java.util.Objects;
import javax.annotation.Nullable;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.pluginapi.Sdk;

/**
 * Identifies a sandbox, like {@code SandboxManager.SandboxKey}: tests with equal specs share a
 * sandbox.
 */
public final class SandboxSpec {
  final InstrumentationConfiguration classLoaderConfig;
  final Sdk sdk;
  final ResourcesMode.Mode resourcesMode;
  final LooperMode.Mode looperMode;
  final GraphicsMode.Mode graphicsMode;

  /**
   * The {@link SandboxManager} of a JUnit 4 runner that has an injector of its own, such as one
   * that binds another {@code TestEnvironmentSpec}, or null for the engine's.
   */
  @Nullable final SandboxManager sandboxManager;

  SandboxSpec(
      InstrumentationConfiguration classLoaderConfig,
      Sdk sdk,
      ResourcesMode.Mode resourcesMode,
      LooperMode.Mode looperMode,
      GraphicsMode.Mode graphicsMode,
      @Nullable SandboxManager sandboxManager) {
    this.classLoaderConfig = classLoaderConfig;
    this.sdk = sdk;
    this.resourcesMode = resourcesMode;
    this.looperMode = looperMode;
    this.graphicsMode = graphicsMode;
    this.sandboxManager = sandboxManager;
  }

  public int getApiLevel() {
    return sdk.getApiLevel();
  }

  /** A stable value for the sandbox's unique ID segment. */
  public String key() {
    return "sdk="
        + sdk.getApiLevel()
        + ",looper="
        + looperMode
        + ",graphics="
        + graphicsMode
        + ",resources="
        + resourcesMode
        + ",config="
        + Integer.toHexString(classLoaderConfig.hashCode())
        + (sandboxManager == null
            ? ""
            : ",manager=" + Integer.toHexString(System.identityHashCode(sandboxManager)));
  }

  public String displayName() {
    return "SDK "
        + sdk.getApiLevel()
        + " ("
        + looperMode
        + " looper, "
        + graphicsMode
        + " graphics)";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SandboxSpec)) {
      return false;
    }
    SandboxSpec that = (SandboxSpec) o;
    return classLoaderConfig.equals(that.classLoaderConfig)
        && sdk.getApiLevel() == that.sdk.getApiLevel()
        && resourcesMode == that.resourcesMode
        && looperMode == that.looperMode
        && graphicsMode == that.graphicsMode
        && sandboxManager == that.sandboxManager;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        classLoaderConfig,
        sdk.getApiLevel(),
        resourcesMode,
        looperMode,
        graphicsMode,
        System.identityHashCode(sandboxManager));
  }

  @Override
  public String toString() {
    return key();
  }
}
