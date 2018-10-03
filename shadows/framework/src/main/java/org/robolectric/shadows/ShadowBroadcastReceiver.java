package org.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(BroadcastReceiver.class)
public class ShadowBroadcastReceiver {
  @RealObject BroadcastReceiver receiver;

  private AtomicBoolean abort; // The abort state of the currently processed broadcast

  @Implementation
  protected void abortBroadcast() {
    // TODO probably needs a check to prevent calling this method from ordinary Broadcasts
    abort.set(true);
  }

  @Implementation
  protected void onReceive(Context context, Intent intent) {
    if (abort == null || !abort.get()) {
      receiver.onReceive(context, intent);
    }
  }

  public void onReceive(Context context, Intent intent, AtomicBoolean abort) {
    this.abort = abort;
    onReceive(context, intent);
    // If the underlying receiver has called goAsync(), we should not finish the pending result yet - they'll do that
    // for us.
    if (receiver.getPendingResult() != null) {
      receiver.getPendingResult().finish();
    }
  }
}
