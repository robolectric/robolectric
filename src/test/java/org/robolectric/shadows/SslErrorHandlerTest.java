package org.robolectric.shadows;

import android.webkit.SslErrorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(TestRunners.WithDefaults.class)
public class SslErrorHandlerTest {

  private SslErrorHandler handler;
  private ShadowSslErrorHandler shadow;

  @Before
  public void setUp() throws Exception {
    handler = Robolectric.newInstanceOf(SslErrorHandler.class);
    shadow = Robolectric.shadowOf(handler);
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
