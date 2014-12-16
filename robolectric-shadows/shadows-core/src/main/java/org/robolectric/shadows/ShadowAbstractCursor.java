package org.robolectric.shadows;

import android.database.AbstractCursor;
import android.net.Uri;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {

  @RealObject
  private AbstractCursor realAbstractCursor;

  /**
   * Returns the Uri set by {@code setNotificationUri()}.
   *
   * @return Notification URI.
   */
  public Uri getNotificationUri_Compatibility() {
    return ReflectionHelpers.getField(realAbstractCursor, "mNotifyUri");
  }
}