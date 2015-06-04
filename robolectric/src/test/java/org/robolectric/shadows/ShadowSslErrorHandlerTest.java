package org.robolectric.shadows;

import android.webkit.SslErrorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
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
