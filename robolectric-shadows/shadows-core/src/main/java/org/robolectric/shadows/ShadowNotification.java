package org.robolectric.shadows;

import android.app.Notification;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.R;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow for {@link android.app.Notification}.
 */
@Implements(Notification.class)
public class ShadowNotification {

  @RealObject
  Notification realNotification;

  public CharSequence getContentTitle() {
    return ((TextView) applyContentView().findViewById(R.id.title)).getText();
  }

  public CharSequence getContentText() {
    return ((TextView) applyContentView().findViewById(R.id.text)).getText();
  }

  public CharSequence getContentInfo() {
   return ((TextView) applyContentView().findViewById(R.id.info)).getText();
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    return ((TextView) applyBigContentView().findViewById(R.id.big_text)).getText();
  }

  public CharSequence getBigContentTitle() {
    return ((TextView) applyBigContentView().findViewById(R.id.title)).getText();
  }

  public CharSequence getBigContentText() {
    return ((TextView) applyBigContentView().findViewById(R.id.text)).getText();
  }

  public boolean isWhenShown() {
    return applyContentView().findViewById(R.id.chronometer).getVisibility() == View.VISIBLE
        || applyContentView().findViewById(R.id.time).getVisibility() == View.VISIBLE;
  }

  public ProgressBar getProgressBar() {
    return ((ProgressBar) applyContentView().findViewById(R.id.progress));
  }

  public boolean usesChronometer() {
    return applyContentView().findViewById(R.id.chronometer).getVisibility() == View.VISIBLE;
  }

  private View applyContentView() {
    return realNotification.contentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private View applyBigContentView() {
    return realNotification.bigContentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }
}
