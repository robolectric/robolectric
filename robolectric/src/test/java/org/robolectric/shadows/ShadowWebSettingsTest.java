package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.webkit.WebSettings;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowWebSettings} */
@RunWith(AndroidJUnit4.class)
public final class ShadowWebSettingsTest {

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void setDefaultUserAgent() {
    ShadowWebSettings.setDefaultUserAgent("Chrome/71.0.143.1");

    assertThat(WebSettings.getDefaultUserAgent(context)).isEqualTo("Chrome/71.0.143.1");
  }
}
