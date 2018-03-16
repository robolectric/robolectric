package org.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class ShadowToast {
  private String text;
  private int duration;
  private int gravity;
  private int xOffset;
  private int yOffset;
  private View view;
  private boolean cancelled;

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
    ShadowToast shadowToast = Shadow.extract(toast);
    shadowToast.text = text.toString();
    return toast;
  }

  @Implementation
  public void show() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    shadowApplication.getShownToasts().add(toast);
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
    cancelled = true;
  }

  public boolean isCancelled() {
    return cancelled;
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
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    shadowApplication.getShownToasts().clear();
  }

  /**
   * Returns the number of {@code Toast} requests that have been made during this test run
   * or since {@link #reset()} has been called.
   *
   * @return the number of {@code Toast} requests that have been made during this test run
   *         or since {@link #reset()} has been called.
   */
  public static int shownToastCount() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    return shadowApplication.getShownToasts().size();
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
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    for (Toast toast : shadowApplication.getShownToasts()) {
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
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    for (Toast toast : shadowApplication.getShownToasts()) {
      ShadowToast shadowToast = Shadow.extract(toast);
      String text = shadowToast.text;
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
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    List<Toast> shownToasts = shadowApplication.getShownToasts();
    ShadowToast shadowToast = Shadow.extract(shownToasts.get(shownToasts.size() - 1));
    return (shownToasts.size() == 0) ? null : shadowToast.text;
  }

  /**
   * Returns the most recently shown {@code Toast}.
   *
   * @return the most recently shown {@code Toast}
   */
  public static Toast getLatestToast() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    List<Toast> shownToasts = shadowApplication.getShownToasts();
    return (shownToasts.size() == 0) ? null : shownToasts.get(shownToasts.size() - 1);
  }
}
