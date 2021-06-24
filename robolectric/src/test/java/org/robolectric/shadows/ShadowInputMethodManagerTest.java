package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSubtype.InputMethodSubtypeBuilder;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

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
  public void hideSoftInputFromWindow_shouldNotifiyResult_alreadyHidden() {
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

  @Test
  public void shouldUpdateInputMethodList() {
    InputMethodInfo inputMethodInfo =
        new InputMethodInfo("pkg", "ClassName", "customIME", "customImeSettingsActivity");

    shadow.setInputMethodInfoList(ImmutableList.of(inputMethodInfo));

    assertThat(shadow.getInputMethodList()).containsExactly(inputMethodInfo);
  }

  @Test
  public void getInputMethodListReturnsEmptyListByDefault() {
    assertThat(shadow.getInputMethodList()).isEmpty();
  }

  @Test
  public void shouldUpdateEnabledInputMethodList() {
    InputMethodInfo inputMethodInfo =
        new InputMethodInfo("pkg", "ClassName", "customIME", "customImeSettingsActivity");

    shadow.setEnabledInputMethodInfoList(ImmutableList.of(inputMethodInfo));

    assertThat(shadow.getEnabledInputMethodList()).containsExactly(inputMethodInfo);
  }

  @Test
  public void getEnabledInputMethodListReturnsEmptyListByDefault() {
    assertThat(shadow.getEnabledInputMethodList()).isEmpty();
  }

  @Test
  public void getCurrentInputMethodSubtype_returnsNullByDefault() {
    assertThat(shadow.getCurrentInputMethodSubtype()).isNull();
  }

  /** The builder is only available for 19+. */
  @Config(minSdk = KITKAT)
  @Test
  public void setCurrentInputMethodSubtype_isReturned() {
    InputMethodSubtype inputMethodSubtype = new InputMethodSubtypeBuilder().build();
    shadow.setCurrentInputMethodSubtype(inputMethodSubtype);
    assertThat(manager.getCurrentInputMethodSubtype()).isEqualTo(inputMethodSubtype);
  }

  @Test
  public void sendAppPrivateCommandListenerIsNotified() {
    View expectedView = new View(ApplicationProvider.getApplicationContext());
    String expectedAction = "action";
    Bundle expectedBundle = new Bundle();

    ShadowInputMethodManager.PrivateCommandListener listener =
        new ShadowInputMethodManager.PrivateCommandListener() {
          @Override
          public void onPrivateCommand(View view, String action, Bundle data) {
            assertThat(view).isEqualTo(expectedView);
            assertThat(action).isEqualTo(expectedAction);
            assertThat(data).isEqualTo(expectedBundle);
          }
        };

    shadow.setAppPrivateCommandListener(listener);

    shadow.sendAppPrivateCommand(expectedView, expectedAction, expectedBundle);
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
