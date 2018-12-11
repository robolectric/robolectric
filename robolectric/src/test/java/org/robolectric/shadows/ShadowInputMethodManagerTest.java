package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.inputmethod.InputMethodManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
public class ShadowInputMethodManagerTest {

  private InputMethodManager manager;
  private ShadowInputMethodManager shadow;

  @Before
  public void setUp() throws Exception {
    manager =
        (InputMethodManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
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
  public void hideSoftInputFromWindow_shouldNotifiyResult_hidden() {
    manager.showSoftInput(null, 0);

    CapturingResultReceiver resultReceiver =
        new CapturingResultReceiver(new Handler(Looper.getMainLooper()));
    manager.hideSoftInputFromWindow(null, 0, resultReceiver);
    assertThat(resultReceiver.resultCode).isEqualTo(InputMethodManager.RESULT_HIDDEN);
  }

  @Test
  public void hideSoftInputFromWindow_shouldNotifiyResult_alreadHidden() {
    CapturingResultReceiver resultReceiver =
        new CapturingResultReceiver(new Handler(Looper.getMainLooper()));
    manager.hideSoftInputFromWindow(null, 0, resultReceiver);
    assertThat(resultReceiver.resultCode).isEqualTo(InputMethodManager.RESULT_UNCHANGED_HIDDEN);
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

  private static class CapturingResultReceiver extends ResultReceiver {

    private int resultCode = -1;

    public CapturingResultReceiver(Handler handler) {
      super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      super.onReceiveResult(resultCode, resultData);
      this.resultCode = resultCode;
    }
  }
}
