package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.robolectric.annotation.SQLiteMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLegacySQLiteConnection.convertSQLWithLocalizedUnicodeCollator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
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
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.shadows.util.SQLiteLibraryLoader;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
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
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
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
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
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
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    assertThat(conn).isNotNull();
    assertWithMessage("open").that(conn.isOpen()).isTrue();
  }

  @Test
  public void nativeClose_closesConnection() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    ShadowLegacySQLiteConnection.nativeClose(ptr);
    assertWithMessage("open").that(conn.isOpen()).isFalse();
  }

  @Test
  public void reset_closesConnection() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    ShadowLegacySQLiteConnection.reset();
    assertWithMessage("open").that(conn.isOpen()).isFalse();
  }

  @Test
  public void reset_clearsConnectionCache() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    final Map<Long, SQLiteConnection> connectionsMap =
        ReflectionHelpers.getField(connections, "connectionsMap");

    assertWithMessage("connections before").that(connectionsMap).isNotEmpty();
    ShadowLegacySQLiteConnection.reset();

    assertWithMessage("connections after").that(connectionsMap).isEmpty();
  }

  @Test
  public void reset_clearsStatementCache() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    final Map<Long, SQLiteStatement> statementsMap =
        ReflectionHelpers.getField(connections, "statementsMap");

    assertWithMessage("statements before").that(statementsMap).isNotEmpty();
    ShadowLegacySQLiteConnection.reset();

    assertWithMessage("statements after").that(statementsMap).isEmpty();
  }

  @Test
  public void error_resultsInSpecificExceptionWithCause() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
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
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    Thread.currentThread().interrupt();
    try {
      database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    } finally {
      Thread.interrupted();
    }
    ShadowLegacySQLiteConnection.reset();
  }

  @Test
  public void uniqueConstraintViolation_errorMessage() {
    database.execSQL(
        "CREATE TABLE my_table(\n"
            + "  _id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
            + "  unique_column TEXT UNIQUE\n"
            + ");\n");
    ContentValues values = new ContentValues();
    values.put("unique_column", "test");
    database.insertOrThrow("my_table", null, values);
    SQLiteConstraintException exception =
        assertThrows(
            SQLiteConstraintException.class,
            () -> database.insertOrThrow("my_table", null, values));
    assertThat(exception).hasMessageThat().endsWith("(code 2067 SQLITE_CONSTRAINT_UNIQUE)");
  }

  @Test
  public void test_setUseInMemoryDatabase() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
    assertThat(conn.isMemoryDatabase()).isFalse();
    ShadowSQLiteConnection.setUseInMemoryDatabase(true);
    SQLiteDatabase inMemoryDb = createDatabase("in_memory.db");
    SQLiteConnection inMemoryConn = getSQLiteConnection();
    assertThat(inMemoryConn.isMemoryDatabase()).isTrue();
    inMemoryDb.close();
  }

  @Test
  public void cancel_shouldCancelAllStatements() {
    assume().that(SQLiteLibraryLoader.isOsSupported()).isTrue();
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
            databasePath.getPath(), 0, "test connection", false, false);
    connections =
        ReflectionHelpers.getStaticField(ShadowLegacySQLiteConnection.class, "CONNECTIONS");
    return connections.getConnection(ptr);
  }
}
