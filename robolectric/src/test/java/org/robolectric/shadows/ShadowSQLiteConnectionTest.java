package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.annotation.SQLiteMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLegacySQLiteConnection.convertSQLWithLocalizedUnicodeCollator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatatypeMismatchException;
import android.database.sqlite.SQLiteStatement;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.almworks.sqlite4java.SQLiteConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.shadows.util.SQLiteLibraryLoader;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
@SQLiteMode(LEGACY) // This test relies on legacy SQLite behavior in Robolectric.
public class ShadowSQLiteConnectionTest {
  private SQLiteDatabase database;
  private File databasePath;
  private long ptr;
  private SQLiteConnection conn;
  private ShadowLegacySQLiteConnection.Connections connections;

  @Before
  public void setUp() throws Exception {
    if (!SQLiteLibraryLoader.isOsSupported()) {
      return;
    }
    database = createDatabase("database.db");
    SQLiteStatement createStatement =
        database.compileStatement(
            "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR ,"
                + " `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement.execute();
    conn = getSQLiteConnection();
  }

  @After
  public void tearDown() {
    if (!SQLiteLibraryLoader.isOsSupported()) {
      return;
    }
    database.close();
  }

  @Test
  public void testSqlConversion() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine`"))
        .isEqualTo("select * from `routine`");

    assertThat(
            convertSQLWithLocalizedUnicodeCollator(
                "select * from `routine` order by name \n\r \f collate\f\n\tunicode"
                    + "\n, id \n\n\t collate\n\t \n\flocalized"))
        .isEqualTo(
            "select * from `routine` order by name COLLATE NOCASE\n" + ", id COLLATE NOCASE");

    assertThat(
            convertSQLWithLocalizedUnicodeCollator(
                "select * from `routine` order by name" + " collate localized"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");

    assertThat(
            convertSQLWithLocalizedUnicodeCollator(
                "select * from `routine` order by name" + " collate unicode"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");
  }

  @Test
  public void testSQLWithLocalizedOrUnicodeCollatorShouldBeSortedAsNoCase() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    database.execSQL("insert into routine(name) values ('Hand press 1')");
    database.execSQL("insert into routine(name) values ('hand press 2')");
    database.execSQL("insert into routine(name) values ('Hand press 3')");

    List<String> expected =
        Arrays.asList(
            "Hand press" + " 1", "hand press" + " 2", "Hand press" + " 3", "الصحافة" + " اليدوية");
    String sqlLocalized = "SELECT `name` FROM `routine` ORDER BY `name` collate localized";
    String sqlUnicode = "SELECT `name` FROM `routine` ORDER BY `name` collate unicode";

    assertThat(simpleQueryForList(database, sqlLocalized)).isEqualTo(expected);
    assertThat(simpleQueryForList(database, sqlUnicode)).isEqualTo(expected);
  }

  private List<String> simpleQueryForList(SQLiteDatabase db, String sql) {
    Cursor cursor = db.rawQuery(sql, new String[0]);
    List<String> result = new ArrayList<>();
    while (cursor.moveToNext()) {
      result.add(cursor.getString(0));
    }
    cursor.close();
    return result;
  }

  @Test
  public void nativeOpen_addsConnectionToPool() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    assertThat(conn).isNotNull();
    assertWithMessage("open").that(conn.isOpen()).isTrue();
  }

  @Test
  public void nativeClose_closesConnection() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    ShadowLegacySQLiteConnection.nativeClose(ptr);
    assertWithMessage("open").that(conn.isOpen()).isFalse();
  }

  @Test
  public void reset_closesConnection() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    ShadowLegacySQLiteConnection.reset();
    assertWithMessage("open").that(conn.isOpen()).isFalse();
  }

  @Test
  public void reset_clearsConnectionCache() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    final Map<Long, SQLiteConnection> connectionsMap =
        ReflectionHelpers.getField(connections, "connectionsMap");

    assertWithMessage("connections before").that(connectionsMap).isNotEmpty();
    ShadowLegacySQLiteConnection.reset();

    assertWithMessage("connections after").that(connectionsMap).isEmpty();
  }

  @Test
  public void reset_clearsStatementCache() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    final Map<Long, SQLiteStatement> statementsMap =
        ReflectionHelpers.getField(connections, "statementsMap");

    assertWithMessage("statements before").that(statementsMap).isNotEmpty();
    ShadowLegacySQLiteConnection.reset();

    assertWithMessage("statements after").that(statementsMap).isEmpty();
  }

  @Test
  public void error_resultsInSpecificExceptionWithCause() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    try {
      database.execSQL("insert into routine(name) values ('Hand press 1')");
      ContentValues values = new ContentValues(1);
      values.put("rowid", "foo");
      database.update("routine", values, "name='Hand press 1'", null);
      fail();
    } catch (SQLiteDatatypeMismatchException expected) {
      assertThat(expected)
          .hasCauseThat()
          .hasCauseThat()
          .isInstanceOf(com.almworks.sqlite4java.SQLiteException.class);
    }
  }

  @Test
  public void interruption_doesNotConcurrentlyModifyDatabase() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    Thread.currentThread().interrupt();
    try {
      database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    } finally {
      Thread.interrupted();
    }
    ShadowLegacySQLiteConnection.reset();
  }

  @Test
  public void test_setUseInMemoryDatabase() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    assertThat(conn.isMemoryDatabase()).isFalse();
    ShadowSQLiteConnection.setUseInMemoryDatabase(true);
    SQLiteDatabase inMemoryDb = createDatabase("in_memory.db");
    SQLiteConnection inMemoryConn = getSQLiteConnection();
    assertThat(inMemoryConn.isMemoryDatabase()).isTrue();
    inMemoryDb.close();
  }

  @Test
  public void cancel_shouldCancelAllStatements() {
    assumeTrue(SQLiteLibraryLoader.isOsSupported());
    SQLiteStatement statement1 =
        database.compileStatement("insert into routine(name) values ('Hand press 1')");
    SQLiteStatement statement2 =
        database.compileStatement("insert into routine(name) values ('Hand press 2')");
    ShadowLegacySQLiteConnection.nativeCancel(ptr);
    // An attempt to execute a statement after a cancellation should be a no-op, unless the
    // statement hasn't been cancelled, in which case it will throw a SQLiteInterruptedException.
    statement1.execute();
    statement2.execute();
  }

  private SQLiteDatabase createDatabase(String filename) {
    databasePath = ApplicationProvider.getApplicationContext().getDatabasePath(filename);
    databasePath.getParentFile().mkdirs();
    return SQLiteDatabase.openOrCreateDatabase(databasePath.getPath(), null);
  }

  private SQLiteConnection getSQLiteConnection() {
    ptr =
        ShadowLegacySQLiteConnection.nativeOpen(
                databasePath.getPath(), 0, "test connection", false, false)
            .longValue();
    connections =
        ReflectionHelpers.getStaticField(ShadowLegacySQLiteConnection.class, "CONNECTIONS");
    return connections.getConnection(ptr);
  }
}
