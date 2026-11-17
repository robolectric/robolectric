package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.media.IAudioService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(className = "android.media.PlayerBase", isInAndroidSdk = false)
public class ShadowPlayerBase {

  @Implementation(minSdk = O)
  public static IAudioService getService() {
    return ReflectionHelpers.createNullProxy(IAudioService.class);
  }
}
