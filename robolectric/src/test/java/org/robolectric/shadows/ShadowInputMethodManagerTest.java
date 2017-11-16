package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowInputMethodManagerTest {

  private InputMethodManager manager;
  private ShadowInputMethodManager shadow;

  @Before
  public void setUp() throws Exception {
    manager = (InputMethodManager) RuntimeEnvironment.application.getSystemService(Activity.INPUT_METHOD_SERVICE);
    shadow = Shadows.shadowOf(manager);
  }

  @Test
  public void shouldRecordSoftInputVisibility() {
    assertThat(shadow.isSoftInputVisible()).isFalse();

    manager.showSoftInput(null, 0);
    assertThat(shadow.isSoftInputVisible()).isTrue();

    manager.hideSoftInputFromWindow(null, 0);
    assertThat(shadow.isSoftInputVisible()).isFalse();
  }

  @Test
  public void shouldToggleSoftInputVisibility() {
    assertThat(shadow.isSoftInputVisible()).isFalse();

    manager.toggleSoftInput(0, 0);
    assertThat(shadow.isSoftInputVisible()).isTrue();

    manager.toggleSoftInput(0, 0);
    assertThat(shadow.isSoftInputVisible()).isFalse();
  }

  @Test
  public void shouldNotifyHandlerWhenVisibilityChanged() {
    ShadowInputMethodManager.SoftInputVisibilityChangeHandler mockHandler =
        mock(ShadowInputMethodManager.SoftInputVisibilityChangeHandler.class);
    shadow.setSoftInputVisibilityHandler(mockHandler);
    assertThat(shadow.isSoftInputVisible()).isFalse();

    manager.toggleSoftInput(0, 0);
    verify(mockHandler).handleSoftInputVisibilityChange(true);
  }
}
