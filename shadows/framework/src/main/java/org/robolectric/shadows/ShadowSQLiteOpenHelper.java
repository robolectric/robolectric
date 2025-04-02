package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.database.sqlite.SQLiteDatabase.OpenParams;
import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Avoid calls to setIdleConnectionTimeout. They shouldn't matter for tests, but sometimes induced
 * deadlocks.
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {

  @RealObject private SQLiteOpenHelper realSQLiteOpenHelper;

  public static final long IDLE_CONNECTION_TIMEOUT_DISABLED = -1L;

  private long idleConnectionTimeoutMs = IDLE_CONNECTION_TIMEOUT_DISABLED;

  public long getIdleConnectionTimeout() {
    return idleConnectionTimeoutMs;
  }

  @RequiresApi(api = P)
  public OpenParams getOpenParams() {
    OpenParams.Builder openParamsBuilder =
        reflector(SQLiteOpenHelperReflector.class, realSQLiteOpenHelper).getOpenParamsBuilder();
    if (idleConnectionTimeoutMs != IDLE_CONNECTION_TIMEOUT_DISABLED) {
      // Add the idle connection timeout (see setIdleConnectionTimeout()).
      // Copy the builder to avoid modifying the real object.
      openParamsBuilder =
          new OpenParams.Builder(openParamsBuilder.build())
              .setIdleConnectionTimeout(idleConnectionTimeoutMs);
    }
    return openParamsBuilder.build();
  }

  @Implementation(minSdk = O_MR1)
  protected void setIdleConnectionTimeout(long idleConnectionTimeoutMs) {
    // Calling the real one currently results in a Robolectric deadlock.
    // See https://github.com/robolectric/robolectric/issues/6853.
    this.idleConnectionTimeoutMs = idleConnectionTimeoutMs;
  }

  @Implementation(minSdk = P)
  protected void setOpenParams(OpenParams openParams) {
    this.idleConnectionTimeoutMs = openParams.getIdleConnectionTimeout();
    if (openParams.getIdleConnectionTimeout() != IDLE_CONNECTION_TIMEOUT_DISABLED) {
      // Remove the idle connection timeout (see setIdleConnectionTimeout()).
      // The copy constructor doesn't copy the idle connection timeout.
      openParams = new OpenParams.Builder(openParams).build();
    }
    reflector(SQLiteOpenHelperReflector.class, realSQLiteOpenHelper).setOpenParams(openParams);
  }

  /** Accessor interface for {@link SQLiteOpenHelper}'s internals. */
  @ForType(SQLiteOpenHelper.class)
  private interface SQLiteOpenHelperReflector {
    @Direct
    void setOpenParams(OpenParams openParams);

    @Accessor("mOpenParamsBuilder")
    OpenParams.Builder getOpenParamsBuilder();
  }
}
