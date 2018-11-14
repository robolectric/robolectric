package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.webkit.SslErrorHandler;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowSslErrorHandlerTest {

  private SslErrorHandler handler;
  private ShadowSslErrorHandler shadow;

  @Before
  public void setUp() throws Exception {
    handler = Shadow.newInstanceOf(SslErrorHandler.class);
    shadow = Shadows.shadowOf(handler);
  }

  @Test
  public void shouldInheritFromShadowHandler() {
    assertThat(shadow).isInstanceOf(ShadowHandler.class);
  }

  @Test
  public void shouldRecordCancel() {
    assertThat(shadow.wasCancelCalled()).isFalse();
    handler.cancel();
    assertThat(shadow.wasCancelCalled()).isTrue();
  }

  @Test
  public void shouldRecordProceed() {
    assertThat(shadow.wasProceedCalled()).isFalse();
    handler.proceed();
    assertThat(shadow.wasProceedCalled()).isTrue();
  }
}
