package org.robolectric.shadows;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import java.net.Socket;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(VpnService.class)
public class ShadowVpnService extends ShadowService {

  private static Intent prepareIntent = new Intent();

  /** @see #setPrepareResult(Intent). */
  @Implementation
  protected static Intent prepare(Context context) {
    return prepareIntent;
  }

  /** Sets the return value of #prepare(Context). */
  public static void setPrepareResult(Intent intent) {
    prepareIntent = intent;
  }

  /**
   * No-ops and always return true, override to avoid call to non-existent Socket.getFileDescriptor.
   */
  @Implementation
  protected boolean protect(Socket socket) {
    return true;
  }

  @Resetter
  public static synchronized void reset() {
    prepareIntent = new Intent();
  }
}
