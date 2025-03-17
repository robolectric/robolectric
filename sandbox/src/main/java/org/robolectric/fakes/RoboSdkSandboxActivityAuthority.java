package org.robolectric.fakes;

import android.content.Context;
import android.content.Intent;

/** Translation class for SdkSandboxActivityAuthority */
public class RoboSdkSandboxActivityAuthority {
  public static boolean isSdkSandboxActivityIntent(Context context, Intent intent) {
    return false;
  }
}
