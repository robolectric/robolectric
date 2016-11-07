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
import org.robolectric.Shadows;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadows.ResourceHelper.getInternalResourceId;

/**
 * Shadow for {@link android.app.Notification}.
 */
@Implements(Notification.class)
public class ShadowNotification {

  @RealObject
  Notification realNotification;

  public CharSequence getContentTitle() {
    return findText(applyContentView(), "title");
  }

  public CharSequence getContentText() {
    return findText(applyContentView(), "text");
  }

  public CharSequence getContentInfo() {
    String resourceName = getApiLevel() >= N ? "header_text" : "info";
    return findText(applyContentView(), resourceName);
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    return findText(applyBigContentView(), "big_text");
  }

  public CharSequence getBigContentTitle() {
    return findText(applyBigContentView(), "title");
  }

  public CharSequence getBigContentText() {
    String resourceName = getApiLevel() >= N ? "header_text" : "text";
    return findText(applyBigContentView(), resourceName);
  }

  public Bitmap getBigPicture() {
    ImageView imageView = (ImageView) applyBigContentView().findViewById(getInternalResourceId("big_picture"));
    return imageView != null && imageView.getDrawable() != null
        ? ((BitmapDrawable) imageView.getDrawable()).getBitmap() : null;
  }

  public boolean isWhenShown() {
    return findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE
        || findView(applyContentView(), "time").getVisibility() == View.VISIBLE;
  }

  public ProgressBar getProgressBar() {
    return ((ProgressBar) findView(applyContentView(), "progress"));
  }

  public boolean usesChronometer() {
    return findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE;
  }

  private View applyContentView() {
    return realNotification.contentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private View applyBigContentView() {
    return realNotification.bigContentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private CharSequence findText(View view, String resourceName) {
    TextView textView = (TextView) findView(view, resourceName);
    return textView.getText();
  }

  private View findView(View view, String resourceName) {
    View subView = view.findViewById(getInternalResourceId(resourceName));
    if (subView == null) {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      Shadows.shadowOf(view).dump(new PrintStream(buf), 4);
      throw new IllegalArgumentException("no id." + resourceName + " found in view:\n" + buf.toString());
    }
    return subView;
  }
}
