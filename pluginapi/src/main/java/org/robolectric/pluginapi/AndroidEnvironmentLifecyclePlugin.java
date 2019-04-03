package org.robolectric.pluginapi;

/**
 * Plugin which allows behaviour extension in AndroidEnvironment
 */
public interface AndroidEnvironmentLifecyclePlugin {

  /**
   * Runs additional setup during AndroidEnvironment.SetupApplicationState
   */
  void onSetupApplicationState();
}
