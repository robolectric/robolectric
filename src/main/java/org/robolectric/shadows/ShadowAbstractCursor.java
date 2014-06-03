package org.robolectric.shadows;

import android.database.AbstractCursor;
import android.net.Uri;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.fest.reflect.core.Reflection.field;

@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {

  @RealObject
  private AbstractCursor realAbstractCursor;

  /**
   * Returns the Uri set by {@code setNotificationUri()}.  Method included for testing
   * pre-API 11 projects.
   */
  public Uri getNotificationUri_Compatibility() {
    return field("mNotifyUri").ofType(Uri.class).in(realAbstractCursor).get();
  }


}