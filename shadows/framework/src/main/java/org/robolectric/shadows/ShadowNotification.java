package org.robolectric.shadows;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.graphics.Bitmap;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Notification.class)
@SuppressLint("NewApi")
public class ShadowNotification {

  @RealObject Notification realNotification;

  public CharSequence getContentTitle() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_TITLE);
  }

  public CharSequence getContentText() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_TEXT);
  }

  public CharSequence getContentInfo() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT)
        == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
  }

  public CharSequence getBigContentTitle() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
  }

  public CharSequence getBigContentText() {
    return realNotification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
  }

  public List<CharSequence> getTextLines() {
    CharSequence[] linesArray =
        realNotification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
    if (linesArray == null) {
      return ImmutableList.of();
    }
    return ImmutableList.copyOf(linesArray);
  }

  public Bitmap getBigPicture() {
    return realNotification.extras.getParcelable(Notification.EXTRA_PICTURE);
  }

  public boolean isWhenShown() {
    return realNotification.extras.getBoolean(Notification.EXTRA_SHOW_WHEN);
  }

  public boolean isIndeterminate() {
    return realNotification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE);
  }

  public int getMax() {
    return realNotification.extras.getInt(Notification.EXTRA_PROGRESS_MAX);
  }

  public int getProgress() {
    return realNotification.extras.getInt(Notification.EXTRA_PROGRESS);
  }

  public boolean usesChronometer() {
    return realNotification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER);
  }
}
