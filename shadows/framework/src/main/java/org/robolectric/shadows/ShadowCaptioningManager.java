package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.accessibility.CaptioningManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link android.view.accessibility.CaptioningManager}. */
@Implements(CaptioningManager.class)
public class ShadowCaptioningManager {
  @RealObject private CaptioningManager realCaptioningManager;

  @Implementation(minSdk = TIRAMISU)
  protected void setSystemAudioCaptioningEnabled(boolean isEnabled) {
    Settings.Secure.putInt(
        getContentResolver(), Settings.Secure.ODI_CAPTIONS_ENABLED, isEnabled ? 1 : 0);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setSystemAudioCaptioningUiEnabled(boolean isEnabled) {
    Settings.Secure.putInt(
        getContentResolver(), Settings.Secure.ODI_CAPTIONS_VOLUME_UI_ENABLED, isEnabled ? 1 : 0);
  }

  @Implementation(minSdk = TIRAMISU)
  protected boolean isSystemAudioCaptioningUiEnabled() {
    return Settings.Secure.getInt(
            getContentResolver(), Settings.Secure.ODI_CAPTIONS_VOLUME_UI_ENABLED, 1)
        == 1;
  }

  private ContentResolver getContentResolver() {
    return reflector(CaptioningManagerReflector.class, realCaptioningManager).getContentResolver();
  }

  @ForType(CaptioningManager.class)
  interface CaptioningManagerReflector {
    @Accessor("mContentResolver")
    ContentResolver getContentResolver();
  }
}
