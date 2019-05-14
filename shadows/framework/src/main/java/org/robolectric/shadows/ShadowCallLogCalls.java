package org.robolectric.shadows;

import android.content.Context;
import android.provider.CallLog;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for the system's CallLog.Call class that allows tests to configure the most recent call.
 */
@Implements(CallLog.Calls.class)
public class ShadowCallLogCalls {
  private static String lastOutgoingCall;

  /**
   * Gets the last outgoing call String set by {@link #setLastOutgoingCall(String)}.
   *
   * @param context A Context object not used
   * @return The last outgoing call set by {@link #setLastOutgoingCall(String)}
   */
  @Implementation
  protected static String getLastOutgoingCall(Context context) {
    return lastOutgoingCall;
  }

  /**
   * Sets a last outgoing call that can later be retrieved by {@link #getLastOutgoingCall(Context)}.
   *
   * @param lastCall The last outgoing call String.
   */
  public static void setLastOutgoingCall(String lastCall) {
    lastOutgoingCall = lastCall;
  }

  @Resetter
  public static void reset() {
    lastOutgoingCall = null;
  }
}
