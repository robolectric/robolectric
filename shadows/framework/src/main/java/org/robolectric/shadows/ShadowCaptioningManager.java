package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.accessibility.CaptioningManager;
import java.util.Locale;
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

  /** Sets the enabled state of the captioning manager. */
  public void setEnabled(boolean enabled) {
    Settings.Secure.putInt(
        getContentResolver(), Settings.Secure.ACCESSIBILITY_CAPTIONING_ENABLED, enabled ? 1 : 0);
  }

  /**
   * Sets the preferred locale of the captioning manager.
   *
   * @param locale The {@link Locale}.
   */
  public void setLocale(Locale locale) {
    Settings.Secure.putString(
        getContentResolver(),
        Settings.Secure.ACCESSIBILITY_CAPTIONING_LOCALE,
        locale.toLanguageTag());
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
