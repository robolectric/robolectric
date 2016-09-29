package org.robolectric.shadows;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.shadows.ResourceHelper.getInternalResourceId;

/**
 * Shadow for {@link android.app.Notification}.
 */
@Implements(Notification.class)
public class ShadowNotification {

  @RealObject
  Notification realNotification;

  public CharSequence getContentTitle() {
    return ((TextView) applyContentView().findViewById(getInternalResourceId("title"))).getText();
  }

  public CharSequence getContentText() {
    return ((TextView) applyContentView().findViewById(getInternalResourceId("text"))).getText();
  }

  public CharSequence getContentInfo() {
   return ((TextView) applyContentView().findViewById(getInternalResourceId("info"))).getText();
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    return ((TextView) applyBigContentView().findViewById(getInternalResourceId("big_text"))).getText();
  }

  public CharSequence getBigContentTitle() {
    return ((TextView) applyBigContentView().findViewById(getInternalResourceId("title"))).getText();
  }

  public CharSequence getBigContentText() {
    return ((TextView) applyBigContentView().findViewById(getInternalResourceId("text"))).getText();
  }

  public Bitmap getBigPicture() {
    ImageView imageView = (ImageView) applyBigContentView().findViewById(getInternalResourceId("big_picture"));
    return imageView !=null && imageView.getDrawable() != null ? ((BitmapDrawable) imageView.getDrawable()).getBitmap() : null;
  }

  public boolean isWhenShown() {
    return applyContentView().findViewById(getInternalResourceId("chronometer")).getVisibility() == View.VISIBLE
        || applyContentView().findViewById(getInternalResourceId("time")).getVisibility() == View.VISIBLE;
  }

  public ProgressBar getProgressBar() {
    return ((ProgressBar) applyContentView().findViewById(getInternalResourceId("progress")));
  }

  public boolean usesChronometer() {
    return applyContentView().findViewById(getInternalResourceId("chronometer")).getVisibility() == View.VISIBLE;
  }

  private View applyContentView() {
    return realNotification.contentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private View applyBigContentView() {
    return realNotification.bigContentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }
}
