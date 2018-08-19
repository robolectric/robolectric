package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadows.ResourceHelper.getInternalResourceId;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(Notification.class)
@SuppressLint("NewApi")
public class ShadowNotification {

  @RealObject
  Notification realNotification;

  public CharSequence getContentTitle() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getCharSequence(Notification.EXTRA_TITLE)
        : findText(applyContentView(), "title");
  }

  public CharSequence getContentText() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getCharSequence(Notification.EXTRA_TEXT)
        : findText(applyContentView(), "text");
  }

  public CharSequence getContentInfo() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
    } else {
      return findText(applyContentView(), "info");
    }
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
    } else {
      return findText(applyBigContentView(), "big_text");
    }
  }

  public CharSequence getBigContentTitle() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
    } else {
      return findText(applyBigContentView(), "title");
    }
  }

  public CharSequence getBigContentText() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
    } else {
      return findText(applyBigContentView(),  "text");
    }
  }

  public Bitmap getBigPicture() {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      return realNotification.extras.getParcelable(Notification.EXTRA_PICTURE);
    } else {
      ImageView imageView = (ImageView) applyBigContentView().findViewById(getInternalResourceId("big_picture"));
      return imageView != null && imageView.getDrawable() != null
          ? ((BitmapDrawable) imageView.getDrawable()).getBitmap() : null;
    }
  }

  public boolean isWhenShown() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_SHOW_WHEN)
        : findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE
        || findView(applyContentView(), "time").getVisibility() == View.VISIBLE;
  }

  private ProgressBar getProgressBar_PreN() {
    return ((ProgressBar) findView(applyContentView(), "progress"));
  }

  public boolean isIndeterminate() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE)
        : getProgressBar_PreN().isIndeterminate();
  }

  public int getMax() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getInt(Notification.EXTRA_PROGRESS_MAX)
        : getProgressBar_PreN().getMax();
  }

  public int getProgress() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getInt(Notification.EXTRA_PROGRESS)
        : getProgressBar_PreN().getProgress();
  }

  public boolean usesChronometer() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)
        : findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE;
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
      ShadowView shadowView = Shadow.extract(view);
      shadowView.dump(new PrintStream(buf), 4);
      throw new IllegalArgumentException("no id." + resourceName + " found in view:\n" + buf.toString());
    }
    return subView;
  }
}
