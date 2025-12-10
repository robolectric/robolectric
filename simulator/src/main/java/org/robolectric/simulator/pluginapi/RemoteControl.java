package org.robolectric.simulator.pluginapi;

import android.app.UiAutomation;
import android.os.Looper;
import java.time.Duration;

/** Connects a Robolectric process to a remote control. */
public interface RemoteControl {
  /** Connects the RemoteControl to the Simulator. */
  void connect(UiAutomation uiAutomation, Looper looper);

  /**
   * Informs the RemoteControl that the Simulator has gone through another cycle.
   *
   * <p>Since this is called in every cycle, it needs to be *fast*. Use it to signal other threads.
   *
   * @param uptimeNanos Current uptime.
   */
  void onCycle(long uptimeNanos);

  /** Disconnects the RemoteControl, shutting down any running jobs within the timeout. */
  void disconnect(Duration timeout);
}
