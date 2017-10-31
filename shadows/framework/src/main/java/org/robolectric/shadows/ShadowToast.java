package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class ShadowToast {
  private String text;
  private int duration;
  private int gravity;
  private int xOffset;
  private int yOffset;
  private View view;

  @RealObject Toast toast;

  @Implementation
  public void __constructor__(Context context) {}

  @Implementation
  public static Toast makeText(Context context, int resId, int duration) {
    return makeText(context, context.getResources().getString(resId), duration);
  }

  @Implementation
  public static Toast makeText(Context context, CharSequence text, int duration) {
    Toast toast = new Toast(context);
    toast.setDuration(duration);
    shadowOf(toast).text = text.toString();
    return toast;
  }

  @Implementation
  public void show() {
    shadowOf(RuntimeEnvironment.application).getShownToasts().add(toast);
  }

  @Implementation
  public void setText(int resId) {
    this.text = RuntimeEnvironment.application.getString(resId);
  }

  @Implementation
  public void setText(CharSequence text) {
    this.text = text.toString();
  }

  @Implementation
  public void setView(View view) {
    this.view = view;
  }

  @Implementation
  public View getView() {
    return view;
  }

  @Implementation
  public void setGravity(int gravity, int xOffset, int yOffset) {
    this.gravity = gravity;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  @Implementation
  public int getGravity() {
    return gravity;
  }

  @Implementation
  public int getXOffset() {
    return xOffset;
  }

  @Implementation
  public int getYOffset() {
    return yOffset;
  }

  @Implementation
  public void setDuration(int duration) {
    this.duration = duration;
  }

  @Implementation
  public int getDuration() {
    return duration;
  }

  @Implementation
  public void cancel() {
  }

  /**
   * Discards the recorded {@code Toast}s. Shown toasts are automatically cleared between
   * tests. This method allows the user to discard recorded toasts during the test in order to make assertions clearer
   * e.g:
   *
   * <pre>
   *
   *   // Show a single toast
   *   myClass.showToast();
   *
   *   assertThat(ShadowToast.shownToastCount()).isEqualTo(1);
   *   ShadowToast.reset();
   *
   *    // Show another toast
   *   myClass.showToast();
   *
   *   assertThat(ShadowToast.shownToastCount()).isEqualTo(1);
   *
   * </pre>
   */
  public static void reset() {
    shadowOf(RuntimeEnvironment.application).getShownToasts().clear();
  }

  /**
   * Returns the number of {@code Toast} requests that have been made during this test run
   * or since {@link #reset()} has been called.
   *
   * @return the number of {@code Toast} requests that have been made during this test run
   *         or since {@link #reset()} has been called.
   */
  public static int shownToastCount() {
    return shadowOf(RuntimeEnvironment.application).getShownToasts().size();
  }

  /**
   * Returns whether or not a particular custom {@code Toast} has been shown.
   *
   * @param message the message to search for
   * @param layoutResourceIdToCheckForMessage
   *                the id of the resource that contains the toast messages
   * @return whether the {@code Toast} was requested
   */
  public static boolean showedCustomToast(CharSequence message, int layoutResourceIdToCheckForMessage) {
    for (Toast toast : shadowOf(RuntimeEnvironment.application).getShownToasts()) {
      String text = ((TextView) toast.getView().findViewById(layoutResourceIdToCheckForMessage)).getText().toString();
      if (text.equals(message.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * query method that returns whether or not a particular {@code Toast} has been shown.
   *
   * @param message the message to search for
   * @return whether the {@code Toast} was requested
   */
  public static boolean showedToast(CharSequence message) {
    for (Toast toast : shadowOf(RuntimeEnvironment.application).getShownToasts()) {
      String text = shadowOf(toast).text;
      if (text != null && text.equals(message.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the text of the most recently shown {@code Toast}.
   *
   * @return the text of the most recently shown {@code Toast}
   */
  public static String getTextOfLatestToast() {
    List<Toast> shownToasts = shadowOf(RuntimeEnvironment.application).getShownToasts();
    return (shownToasts.size() == 0) ? null : shadowOf(shownToasts.get(shownToasts.size() - 1)).text;
  }

  /**
   * Returns the most recently shown {@code Toast}.
   *
   * @return the most recently shown {@code Toast}
   */
  public static Toast getLatestToast() {
    List<Toast> shownToasts = shadowOf(RuntimeEnvironment.application).getShownToasts();
    return (shownToasts.size() == 0) ? null : shownToasts.get(shownToasts.size() - 1);
  }
}
