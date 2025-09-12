package org.robolectric.simulator.pluginapi;

import android.app.UiAutomation;
import android.os.Looper;
import com.google.auto.service.AutoService;
import java.time.Duration;
import javax.annotation.Priority;

/** The default [RemoteControl]. */
@Priority(Integer.MIN_VALUE)
@AutoService(RemoteControl.class)
final class NoOpRemoteControl implements RemoteControl {
  @Override
  public void connect(UiAutomation uiAutomation, Looper looper) {
    // Do nothing
  }

  @Override
  public void disconnect(Duration timeout) {
    // Do nothing
  }
}
