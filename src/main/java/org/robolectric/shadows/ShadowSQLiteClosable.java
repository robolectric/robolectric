package org.robolectric.shadows;

import android.database.sqlite.SQLiteClosable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for  {@code SQLiteClosable}.  Though {@code SQLiteClosable} is an abstract
 * class, a shadow is necessary. Its methods that are overridden in subclasses
 * require this shadow in order to be properly instrumented.
 */
@Implements(SQLiteClosable.class)
public class ShadowSQLiteClosable {

  @Implementation
  public void close() {
  }

}