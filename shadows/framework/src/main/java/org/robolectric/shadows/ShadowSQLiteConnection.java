package org.robolectric.shadows;

import android.database.sqlite.SQLiteConnection;
import android.os.SystemProperties;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.config.ConfigurationRegistry;

/**
 * The base class of {@link ShadowNativeSQLiteConnection}, the shadow of {@link SQLiteConnection},
 * with SQLite settings for tests.
 */
public class ShadowSQLiteConnection {

  /**
   * Was only supported in the legacy SQLite mode, which was removed in Robolectric 4.19.
   *
   * @deprecated SQLite databases are native, and this always throws {@link
   *     UnsupportedOperationException}.
   */
  @Deprecated
  public static void setUseInMemoryDatabase(boolean value) {
    throw new UnsupportedOperationException(
        "setUseInMemoryDatabase was only supported in the legacy SQLite mode, which was removed in"
            + " Robolectric 4.19.");
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
   * <p>This also updates the default sync mode used when SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING
   * (WAL) is used.
   *
   * <p>If your test expects SQLite files being synced to disk, such as having multiple processes
   * interact with the database, or deleting SQLite files while connections are open and having this
   * reflected in the open connection, use "FULL" mode.
   */
  public static void setDefaultSyncMode(String value) {
    SystemProperties.set("debug.sqlite.syncmode", value);
    SystemProperties.set("debug.sqlite.wal.syncmode", value);
  }

  /**
   * Sets the default journal mode for SQLite databases. Robolectric uses "MEMORY" by default in
   * order to improve SQLite performance. The Android default is <code>PERSIST</code> in SDKs <= 25
   * and <code>TRUNCATE</code> in SDKs > 25.
   *
   * <p>Similarly to {@link #setDefaultSyncMode}, if your test expects SQLite rollback journal to be
   * synced to disk, use <code>PERSIST</code> or <code>TRUNCATE</code>.
   */
  public static void setDefaultJournalMode(String value) {
    SystemProperties.set("debug.sqlite.journalmode", value);
  }
}
