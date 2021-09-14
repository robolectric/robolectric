package org.robolectric.shadows;

import android.database.sqlite.SQLiteConnection;
import android.os.SystemProperties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.SQLiteMode.Mode;
import org.robolectric.config.ConfigurationRegistry;

/**
 * The base shadow class for {@link SQLiteConnection} shadow APIs.
 *
 * <p>The actual shadow class for {@link SQLiteConnection} will be selected during runtime by the
 * Picker.
 */
@Implements(
    className = "android.database.sqlite.SQLiteConnection",
    isInAndroidSdk = false,
    shadowPicker = ShadowSQLiteConnection.Picker.class)
public class ShadowSQLiteConnection {

  protected static AtomicBoolean useInMemoryDatabase = new AtomicBoolean();

  /** Shadow {@link Picker} for {@link ShadowSQLiteConnection} */
  public static class Picker extends SQLiteShadowPicker<ShadowSQLiteConnection> {
    public Picker() {
      super(ShadowLegacySQLiteConnection.class, ShadowNativeSQLiteConnection.class);
    }
  }

  public static void setUseInMemoryDatabase(boolean value) {
    if (sqliteMode() == Mode.LEGACY) {
      useInMemoryDatabase.set(value);
    } else {
      throw new UnsupportedOperationException(
          "this action is not supported in " + sqliteMode() + " mode.");
    }
  }

  public static SQLiteMode.Mode sqliteMode() {
    return ConfigurationRegistry.get(SQLiteMode.Mode.class);
  }

  /**
   * Sets the default sync mode for SQLite databases. Robolectric uses "OFF" by default in order to
   * improve SQLite performance. The Android default is "FULL" in order to be more resilient to
   * process crashes. However, this is not a requirement for Robolectric processes, where all
   * database files are temporary and get deleted after each test.
   *
   * <p>If your test expects SQLite files being synced to disk, such as having multiple processes
   * interact with the database, or deleting SQLite files while connections are open and having this
   * reflected in the open connection, use "FULL" mode.
   */
  public static void setDefaultSyncMode(String value) {
    SystemProperties.set("debug.sqlite.syncmode", value);
  }

  /**
   * Sets the default sync mode for SQLite databases when SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING
   * is used. Robolectric uses "OFF" by default in order to improve SQLite performance. The Android
   * default is "FULL" for SDKs < 28 and "NORMAL" for SDKs >= 28.
   *
   * <p>If your test expects SQLite files being synced to disk, such as having multiple processes
   * interact with the database, or deleting SQLite files while connections are open and having this
   * reflected in the open connection, use "FULL" mode.
   */
  public static void setDefaultWALSyncMode(String value) {
    SystemProperties.set("debug.sqlite.wal.syncmode", value);
  }

  /**
   * Sets the default journal mode for SQLite databases. Robolectric uses "MEMORY" by default in
   * order to improve SQLite performance. The Android default is <code>PERSIST</code> in SDKs <= 25
   * and <code>TRUNCATE</code> in SDKs > 25.
   *
   * <p>Similarly to {@link setDefaultSyncMode}, if your test expects SQLite rollback journal to be
   * synced to disk, use <code>PERSIST</code> or <code>TRUNCATE</code>.
   */
  public static void setDefaultJournalMode(String value) {
    SystemProperties.set("debug.sqlite.journalmode", value);
  }

  @Resetter
  public static void reset() {
    useInMemoryDatabase.set(false);
  }
}
