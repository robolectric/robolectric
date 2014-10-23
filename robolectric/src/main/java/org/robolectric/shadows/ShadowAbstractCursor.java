package org.robolectric.shadows;

import android.database.AbstractCursor;
import android.net.Uri;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Field;

@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {

  @RealObject
  private AbstractCursor realAbstractCursor;

  /**
   * Returns the Uri set by {@code setNotificationUri()}.  Method included for testing
   * pre-API 11 projects.
   */
  public Uri getNotificationUri_Compatibility() {
    try {
      Field mNotifyUri = AbstractCursor.class.getDeclaredField("mNotifyUri");
      mNotifyUri.setAccessible(true);
      return (Uri) mNotifyUri.get(realAbstractCursor);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}