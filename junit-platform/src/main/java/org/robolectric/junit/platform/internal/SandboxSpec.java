package org.robolectric.junit.platform.internal;

import java.util.Objects;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.pluginapi.Sdk;

/**
 * Identifies a sandbox, like {@code SandboxManager.SandboxKey}: tests with equal specs share a
 * sandbox. The SQLite mode is applied per test and is not part of the identity.
 */
@SuppressWarnings("deprecation") // SQLiteMode is deprecated but still supported.
public final class SandboxSpec {
  final InstrumentationConfiguration classLoaderConfig;
  final Sdk sdk;
  final ResourcesMode.Mode resourcesMode;
  final LooperMode.Mode looperMode;
  final SQLiteMode.Mode sqliteMode;
  final GraphicsMode.Mode graphicsMode;

  SandboxSpec(
      InstrumentationConfiguration classLoaderConfig,
      Sdk sdk,
      ResourcesMode.Mode resourcesMode,
      LooperMode.Mode looperMode,
      SQLiteMode.Mode sqliteMode,
      GraphicsMode.Mode graphicsMode) {
    this.classLoaderConfig = classLoaderConfig;
    this.sdk = sdk;
    this.resourcesMode = resourcesMode;
    this.looperMode = looperMode;
    this.sqliteMode = sqliteMode;
    this.graphicsMode = graphicsMode;
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
        + Integer.toHexString(classLoaderConfig.hashCode());
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
        && graphicsMode == that.graphicsMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        classLoaderConfig, sdk.getApiLevel(), resourcesMode, looperMode, graphicsMode);
  }

  @Override
  public String toString() {
    return key();
  }
}
