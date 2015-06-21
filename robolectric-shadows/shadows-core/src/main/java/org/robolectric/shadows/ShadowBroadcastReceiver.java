package org.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shadow for {@link android.content.BroadcastReceiver}.
 */
@Implements(BroadcastReceiver.class)
public class ShadowBroadcastReceiver {
  @RealObject BroadcastReceiver receiver;

  private AtomicBoolean abort; // The abort state of the currently processed broadcast

  @Implementation
  public void abortBroadcast() {
    // TODO probably needs a check to prevent calling this method from ordinary Broadcasts
    abort.set(true);
  }

  @Implementation
  public void onReceive(Context context, Intent intent) {
    if (abort == null || !abort.get()) {
      receiver.onReceive(context, intent);
    }
  }

  public void onReceive(Context context, Intent intent, AtomicBoolean abort) {
    this.abort = abort;
    onReceive(context, intent);
  }
}
