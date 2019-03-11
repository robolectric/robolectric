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

  @Before
  public void setUp() throws Exception {
    handler = Shadow.newInstanceOf(SslErrorHandler.class);
  }

  @Test
  public void shouldRecordCancel() {
    assertThat(Shadows.shadowOf(handler).wasCancelCalled()).isFalse();
    handler.cancel();
    assertThat(Shadows.shadowOf(handler).wasCancelCalled()).isTrue();
  }

  @Test
  public void shouldRecordProceed() {
    assertThat(Shadows.shadowOf(handler).wasProceedCalled()).isFalse();
    handler.proceed();
    assertThat(Shadows.shadowOf(handler).wasProceedCalled()).isTrue();
  }
}
