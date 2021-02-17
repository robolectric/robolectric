package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.webkit.WebSettings;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowWebSettings} */
@RunWith(AndroidJUnit4.class)
public final class ShadowWebSettingsTest {

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void setDefaultUserAgent() {
    ShadowWebSettings.setDefaultUserAgent("Chrome/71.0.143.1");

    assertThat(WebSettings.getDefaultUserAgent(context)).isEqualTo("Chrome/71.0.143.1");
  }
}
