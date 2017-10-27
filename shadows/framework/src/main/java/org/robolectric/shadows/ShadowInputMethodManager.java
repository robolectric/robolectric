package org.robolectric.shadows;

import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.util.Optional;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = InputMethodManager.class, callThroughByDefault = false)
public class ShadowInputMethodManager {
  /**
   * Handler for receiving soft input visibility changed event.
   *
   * Since Android does not have any API for retrieving soft input status, most application
   * relies on GUI layout changes to detect the soft input change event. Currently, Robolectric are
   * not able to simulate the GUI change when application changes the soft input through {@code
   * InputMethodManager}, this handler can be used by application to simulate GUI change in response
   * of the soft input change.
   */
  @FunctionalInterface
  public interface SoftInputVisibilityChangeHandler {
    void handleSoftInputVisibilityChange(boolean softInputVisible);
  }

  private boolean softInputVisible;
  private Optional<SoftInputVisibilityChangeHandler> visibilityChangeHandler = Optional.empty();

  @HiddenApi @Implementation
  static public InputMethodManager peekInstance() {
    return Shadow.newInstanceOf(InputMethodManager.class);
  }

  @Implementation
  public boolean showSoftInput(View view, int flags) {
    return showSoftInput(view, flags, null);
  }

  @Implementation
  public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
    setSoftInputVisibility(true);
    return true;
  }

  @Implementation
  public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
    return hideSoftInputFromWindow(windowToken, flags, null);
  }

  @Implementation
  public boolean hideSoftInputFromWindow(IBinder windowToken, int flags,
                       ResultReceiver resultReceiver) {
    setSoftInputVisibility(false);
    return true;
  }

  @Implementation
  public void toggleSoftInput(int showFlags, int hideFlags) {
    setSoftInputVisibility(!isSoftInputVisible());
  }

  public boolean isSoftInputVisible() {
    return softInputVisible;
  }

  public void setSoftInputVisibilityHandler(
      SoftInputVisibilityChangeHandler visibilityChangeHandler) {
    this.visibilityChangeHandler =
        Optional.<SoftInputVisibilityChangeHandler>of(visibilityChangeHandler);
  }

  private void setSoftInputVisibility(boolean visible) {
    if (visible == softInputVisible) {
      return;
    }
    softInputVisible = visible;
    if (visibilityChangeHandler.isPresent()) {
      visibilityChangeHandler.get().handleSoftInputVisibilityChange(softInputVisible);
    }
  }
}
