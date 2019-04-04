package org.robolectric.pluginapi;

/**
 * Plugin which allows behaviour extension in TestEnvironment.
 */
public interface TestEnvironmentLifecyclePlugin {

  /**
   * Runs additional setup during TestEnvironment.before().
   */
  void onSetupApplicationState();
}
