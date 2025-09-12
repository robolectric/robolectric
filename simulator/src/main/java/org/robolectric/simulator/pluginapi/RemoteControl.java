package org.robolectric.simulator.pluginapi;

import android.app.UiAutomation;
import android.os.Looper;
import java.time.Duration;

/** Connects a Robolectric process to a remote control. */
public interface RemoteControl {
  void connect(UiAutomation uiAutomation, Looper looper);

  void disconnect(Duration timeout);
}
