package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import java.util.Locale;
import javax.annotation.Nullable;
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

  /**
   * Sets the preferred font scale of the captioning manager.
   *
   * @param fontScale The font scale, where 1 is the default size.
   */
  public void setFontScale(float fontScale) {
    Settings.Secure.putFloat(
        getContentResolver(), Settings.Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE, fontScale);
  }

  /**
   * Sets a custom user caption style, as if the user had chosen custom caption options in the
   * system settings.
   *
   * <p>{@link CaptioningManager#getUserStyle()} then returns a {@link CaptionStyle} with these
   * values.
   *
   * @param foregroundColor The text color.
   * @param backgroundColor The color behind the text.
   * @param edgeType The edge type, for example {@link CaptionStyle#EDGE_TYPE_OUTLINE}.
   * @param edgeColor The edge color.
   * @param windowColor The color of the window that contains the captions.
   * @param typeface The typeface family name, for example {@code "monospace"}, or {@code null} for
   *     the default typeface.
   */
  public void setUserStyle(
      int foregroundColor,
      int backgroundColor,
      int edgeType,
      int edgeColor,
      int windowColor,
      @Nullable String typeface) {
    ContentResolver contentResolver = getContentResolver();
    Settings.Secure.putInt(
        contentResolver,
        Settings.Secure.ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR,
        foregroundColor);
    Settings.Secure.putInt(
        contentResolver,
        Settings.Secure.ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR,
        backgroundColor);
    Settings.Secure.putInt(
        contentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_TYPE, edgeType);
    Settings.Secure.putInt(
        contentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_EDGE_COLOR, edgeColor);
    Settings.Secure.putInt(
        contentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_WINDOW_COLOR, windowColor);
    Settings.Secure.putString(
        contentResolver, Settings.Secure.ACCESSIBILITY_CAPTIONING_TYPEFACE, typeface);
    // Select the custom preset last, so that the style is complete when listeners read it.
    Settings.Secure.putInt(
        contentResolver,
        Settings.Secure.ACCESSIBILITY_CAPTIONING_PRESET,
        CaptionStyle.PRESET_CUSTOM);
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
