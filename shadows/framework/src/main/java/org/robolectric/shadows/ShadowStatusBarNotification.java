package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A shadow implementation of {@link android.service.notification.StatusBarNotification}. */
@Implements(StatusBarNotification.class)
public class ShadowStatusBarNotification {
  @RealObject StatusBarNotification realNotification;
  @Nullable private String key;

  /**
   * Returns the key previously set in {@link #setKey}. If key is null and SDK version is after
   * {@link VERSION_CODES#KITKAT}, call {@link #getKey} on the actual notification. Otherwise
   * returns null.
   */
  @Nullable
  public String getKey() {

    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
      if (key != null) {
        return key;
      }
      return directlyOn(realNotification, StatusBarNotification.class).getKey();
    }

    return null;
  }

  /** Sets the value that may be returned by {@link #getKey()}. */
  public void setKey(@Nullable String key) {
    this.key = key;
  }
}
