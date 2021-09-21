package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.view.accessibility.CaptioningManager;
import android.view.accessibility.CaptioningManager.CaptioningChangeListener;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/** Tests for the ShadowCaptioningManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = 19)
public final class ShadowCaptioningManagerTest {

  @Mock private CaptioningChangeListener captioningChangeListener;

  private CaptioningManager captioningManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    captioningManager =
        (CaptioningManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CAPTIONING_SERVICE);
  }

  @Test
  public void setEnabled_true() {
    assertThat(captioningManager.isEnabled()).isFalse();

    shadowOf(captioningManager).setEnabled(true);

    assertThat(captioningManager.isEnabled()).isTrue();
  }

  @Test
  public void setEnabled_false() {
    shadowOf(captioningManager).setEnabled(false);

    assertThat(captioningManager.isEnabled()).isFalse();
  }

  @Test
  public void setFontScale_changesValueOfGetFontScale() {
    float fontScale = 1.5f;
    shadowOf(captioningManager).setFontScale(fontScale);

    assertThat(captioningManager.getFontScale()).isWithin(0.001f).of(fontScale);
  }

  @Test
  public void setFontScale_notifiesObservers() {
    float fontScale = 1.5f;
    captioningManager.addCaptioningChangeListener(captioningChangeListener);

    shadowOf(captioningManager).setFontScale(fontScale);

    verify(captioningChangeListener).onFontScaleChanged(fontScale);
  }

  @Test
  public void addCaptioningChangeListener_doesNotRegisterSameListenerTwice() {
    float fontScale = 1.5f;
    captioningManager.addCaptioningChangeListener(captioningChangeListener);

    captioningManager.addCaptioningChangeListener(captioningChangeListener);

    shadowOf(captioningManager).setFontScale(fontScale);
    verify(captioningChangeListener).onFontScaleChanged(fontScale);
  }

  @Test
  public void removeCaptioningChangeListener_unregistersFontScaleListener() {
    captioningManager.addCaptioningChangeListener(captioningChangeListener);

    captioningManager.removeCaptioningChangeListener(captioningChangeListener);

    shadowOf(captioningManager).setFontScale(1.5f);
    verifyNoMoreInteractions(captioningChangeListener);
  }

  @Test
  public void setLocale_nonNull() {
    Locale locale = Locale.US;
    assertThat(captioningManager.getLocale()).isNull();

    shadowOf(captioningManager).setLocale(locale);

    assertThat(captioningManager.getLocale()).isEqualTo(locale);
  }

  @Test
  public void setLocale_null() {
    shadowOf(captioningManager).setLocale(null);

    assertThat(captioningManager.getLocale()).isNull();
  }

  @Test
  public void setLocale_notifiesObservers() {
    Locale locale = Locale.US;
    captioningManager.addCaptioningChangeListener(captioningChangeListener);

    shadowOf(captioningManager).setLocale(locale);

    verify(captioningChangeListener).onLocaleChanged(locale);
  }
}
