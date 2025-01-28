package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/** Tests for the ShadowCaptioningManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public final class ShadowCaptioningManagerTest {

  private TestCaptioningChangeListener captioningChangeListener =
      new TestCaptioningChangeListener();

  private static final int ENABLED = 1;
  private static final int DISABLED = 0;

  private CaptioningManager captioningManager;
  private Context context;

  public class TestCaptioningChangeListener extends CaptioningChangeListener {
    public boolean isEnabled = false;
    @Nullable public CaptionStyle captionStyle = null;
    @Nullable public Locale locale = null;
    public float fontScale = 1.0f;
    public boolean systemAudioCaptioningEnabled = false;
    public boolean systemAudioCaptioningUiEnabled = false;

    @Override
    public void onEnabledChanged(boolean enabled) {
      isEnabled = enabled;
    }

    @Override
    public void onUserStyleChanged(@Nonnull CaptionStyle userStyle) {
      captionStyle = userStyle;
    }

    @Override
    public void onLocaleChanged(@Nullable Locale locale) {
      this.locale = locale;
    }

    @Override
    public void onFontScaleChanged(float fontScale) {
      this.fontScale = fontScale;
    }

    @Override
    public void onSystemAudioCaptioningChanged(boolean enabled) {
      this.systemAudioCaptioningEnabled = enabled;
    }

    @Override
    public void onSystemAudioCaptioningUiChanged(boolean enabled) {
      this.systemAudioCaptioningUiEnabled = enabled;
    }
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    captioningManager =
        (CaptioningManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CAPTIONING_SERVICE);
    context = RuntimeEnvironment.getApplication();
  }

  @Test
  public void isEnabled_enabledKeyTrue_returnsTrue() {
    Settings.Secure.putInt(
        context.getContentResolver(), Secure.ACCESSIBILITY_CAPTIONING_ENABLED, ENABLED);

    assertThat(captioningManager.isEnabled()).isTrue();
  }

  @Test
  public void isEnabled_enabledKeyFalse_returnsFalse() {
    Settings.Secure.putInt(
        context.getContentResolver(), Secure.ACCESSIBILITY_CAPTIONING_ENABLED, DISABLED);

    assertThat(captioningManager.isEnabled()).isFalse();
  }

  @Test
  public void isEnabled_afterSetEnabledFalse_returnsFalse() {
    shadowOf(captioningManager).setEnabled(false);

    assertThat(captioningManager.isEnabled()).isFalse();
  }

  @Test
  public void isEnabled_afterSetEnabledTrue_returnsTrue() {
    shadowOf(captioningManager).setEnabled(true);

    assertThat(captioningManager.isEnabled()).isTrue();
  }

  @Test
  public void addCaptioningChangeListener_enabledKeyValueChange_callsCallback() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);
    Settings.Secure.putInt(
        context.getContentResolver(), Secure.ACCESSIBILITY_CAPTIONING_ENABLED, ENABLED);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.isEnabled).isTrue();
  }

  @Test
  public void addCaptioningChangeListener_setEnabledChange_callsCallback() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);
    shadowOf(captioningManager).setEnabled(true);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.isEnabled).isTrue();
  }

  @Test
  public void getFontScale_returnFontScaleKeyValue() {
    Settings.Secure.putFloat(
        context.getContentResolver(), Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE, 2.0f);

    assertThat(captioningManager.getFontScale()).isEqualTo(2.0f);
  }

  @Test
  public void addCaptioningChangeListener_fontScaleKeyValueChange_callsCallback() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);
    Settings.Secure.putFloat(
        context.getContentResolver(), Secure.ACCESSIBILITY_CAPTIONING_FONT_SCALE, 3.0f);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.fontScale).isEqualTo(3.0f);
  }

  @Test
  public void getLocale_returnsLocaleKeyValue() {
    Settings.Secure.putString(
        context.getContentResolver(),
        Secure.ACCESSIBILITY_CAPTIONING_LOCALE,
        Locale.JAPANESE.toLanguageTag());

    assertThat(captioningManager.getLocale()).isEqualTo(Locale.JAPANESE);
  }

  @Test
  public void getLocale_afterSetLocale_returnsLocale() {
    shadowOf(captioningManager).setLocale(Locale.JAPANESE);

    assertThat(captioningManager.getLocale()).isEqualTo(Locale.JAPANESE);
  }

  @Test
  public void getLocale_localKeyValueChanged_callsCallback() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);
    Settings.Secure.putString(
        context.getContentResolver(),
        Secure.ACCESSIBILITY_CAPTIONING_LOCALE,
        Locale.FRENCH.toLanguageTag());

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.locale).isEqualTo(Locale.FRENCH);
  }

  @Test
  public void getLocale_setLocaleChange_callsCallback() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);
    shadowOf(captioningManager).setLocale(Locale.FRENCH);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.locale).isEqualTo(Locale.FRENCH);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setSystemAudioCaptioningEnabled_updatesValue() {
    captioningManager.setSystemAudioCaptioningEnabled(true);

    assertThat(captioningManager.isSystemAudioCaptioningEnabled()).isEqualTo(true);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setSystemAudioCaptioningEnabled_callsCallback() {
    captioningManager.setSystemAudioCaptioningEnabled(false);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.systemAudioCaptioningEnabled).isEqualTo(false);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setSystemAudioCaptioningUiEnabled_updatesValue() {
    captioningManager.setSystemAudioCaptioningUiEnabled(true);

    assertThat(captioningManager.isSystemAudioCaptioningUiEnabled()).isEqualTo(true);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setSystemAudioCaptioningUiEnabled_callsCallback() {
    captioningManager.setSystemAudioCaptioningUiEnabled(false);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(captioningChangeListener.systemAudioCaptioningUiEnabled).isEqualTo(false);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void captioningManager_activityContextEnabled_differentInstancesRetrieveValues() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      CaptioningManager applicationCaptioningManager =
          (CaptioningManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.CAPTIONING_SERVICE);
      Activity activity = controller.get();
      CaptioningManager activityCaptioningManager =
          (CaptioningManager) activity.getSystemService(Context.CAPTIONING_SERVICE);

      boolean applicationCaptioningEnabled =
          applicationCaptioningManager.isSystemAudioCaptioningEnabled();
      boolean activityCaptioningEnabled =
          activityCaptioningManager.isSystemAudioCaptioningEnabled();

      boolean applicationCaptioningUiEnabled =
          applicationCaptioningManager.isSystemAudioCaptioningUiEnabled();
      boolean activityCaptioningUiEnabled =
          activityCaptioningManager.isSystemAudioCaptioningUiEnabled();

      assertThat(applicationCaptioningEnabled).isEqualTo(activityCaptioningEnabled);
      assertThat(applicationCaptioningUiEnabled).isEqualTo(activityCaptioningUiEnabled);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
