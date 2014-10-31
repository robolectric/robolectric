package org.robolectric.shadows;

import android.database.AbstractCursor;
import android.net.Uri;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.ReflectionHelpers;

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
    return ReflectionHelpers.getFieldReflectively(realAbstractCursor, "mNotifyUri");
  }
}