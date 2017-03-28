package org.robolectric.shadows;

import android.media.IAudioService;
import android.media.PlayerBase;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = PlayerBase.class, isInAndroidSdk = false)
public class ShadowPlayerBase {

  @Implementation
  public static IAudioService getService() {
    return ReflectionHelpers.createNullProxy(IAudioService.class);
  }
}
