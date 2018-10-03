package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O_MR1;

import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Avoid calls to setIdleConnectionTimeout.
 * They shouldn't matter for tests, but sometimes induced deadlocks.
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {
  @Implementation(minSdk = O_MR1)
  protected void setIdleConnectionTimeout(long idleConnectionTimeoutMs) {
    // Calling the real one currently results in a Robolectric deadlock. Just ignore it.
    // See b/78464547 .
  }
}
