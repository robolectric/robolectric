package org.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import libcore.util.MutableBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(BroadcastReceiver.class)
public class ShadowBroadcastReceiver {
  @RealObject BroadcastReceiver receiver;

  private MutableBoolean abort; // The abort state of the currently processed broadcast

  @Implementation
  public void abortBroadcast() {
    // TODO probably needs a check to prevent calling this method from ordinary Broadcasts
    abort.value = true;
  }

  @Implementation
  public void onReceive(Context context, Intent intent) {
    if (abort == null || !abort.value) {
      receiver.onReceive(context, intent);
    }
  }

  public void onReceive(Context context, Intent intent, MutableBoolean abort) {
    this.abort = abort;
    onReceive(context, intent);
  }
}
