package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowSQLiteConnection.convertSQLWithLocalizedUnicodeCollator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatatypeMismatchException;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import com.almworks.sqlite4java.SQLiteConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP })
public class ShadowSQLiteConnectionTest {
  private SQLiteDatabase database;
  private File databasePath;
  private long ptr;
  private SQLiteConnection conn;
  private ShadowSQLiteConnection.Connections CONNECTIONS;
  
  @Before
  public void setUp() throws Exception {
    databasePath = RuntimeEnvironment.application.getDatabasePath("database.db");
    databasePath.getParentFile().mkdirs();

    database = SQLiteDatabase.openOrCreateDatabase(databasePath.getPath(), null);
    SQLiteStatement createStatement = database.compileStatement(
        "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement.execute();
    ptr = ShadowSQLiteConnection.nativeOpen(databasePath.getPath(), 0, "test connection", false, false);
    CONNECTIONS = ReflectionHelpers.getStaticField(ShadowSQLiteConnection.class, "CONNECTIONS");
    conn = CONNECTIONS.getConnection(ptr);
  }

  @After
  public void tearDown() throws Exception {
    database.close();
  }

  @Test
  public void testSqlConversion() {
    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine`"))
        .isEqualTo("select * from `routine`");

    assertThat(convertSQLWithLocalizedUnicodeCollator(
        "select * from `routine` order by name \n\r \f collate\f\n\tunicode"
        + "\n, id \n\n\t collate\n\t \n\flocalized"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE\n"
            + ", id COLLATE NOCASE");

    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine` order by name collate localized"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");

    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine` order by name collate unicode"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");
  }

  @Test
  public void testSQLWithLocalizedOrUnicodeCollatorShouldBeSortedAsNoCase() throws Exception {
    database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    database.execSQL("insert into routine(name) values ('Hand press 1')");
    database.execSQL("insert into routine(name) values ('hand press 2')");
    database.execSQL("insert into routine(name) values ('Hand press 3')");

    List<String> expected = Arrays.asList("Hand press 1", "hand press 2", "Hand press 3", "الصحافة اليدوية" );
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
    assertThat(conn).isNotNull();
    assertThat(conn.isOpen()).as("open").isTrue();
  }
    
  @Test
  public void nativeClose_closesConnection() {
    ShadowSQLiteConnection.nativeClose(ptr);
    assertThat(conn.isOpen()).as("open").isFalse();
  }
    
  @Test
  public void reset_closesConnection() {
    ShadowSQLiteConnection.reset();
    assertThat(conn.isOpen()).as("open").isFalse();
  }

  @Test
  public void reset_clearsConnectionCache() {
    final Map<Long, SQLiteConnection> connectionsMap = ReflectionHelpers.getField(CONNECTIONS, "connectionsMap");

    assertThat(connectionsMap).as("connections before").isNotEmpty();
    ShadowSQLiteConnection.reset();

    assertThat(connectionsMap).as("connections after").isEmpty();
  }
  
  @Test
  public void reset_clearsStatementCache() {
    final Map<Long, SQLiteStatement> statementsMap = ReflectionHelpers.getField(CONNECTIONS, "statementsMap");

    assertThat(statementsMap).as("statements before").isNotEmpty();
    ShadowSQLiteConnection.reset();

    assertThat(statementsMap).as("statements after").isEmpty();
  }

  @Test
  public void error_resultsInSpecificExceptionWithCause() {
    try {
      database.execSQL("insert into routine(name) values ('Hand press 1')");
      ContentValues values = new ContentValues(1);
      values.put("rowid", "foo");
      database.update("routine", values, "name='Hand press 1'", null);
      fail();
    } catch (SQLiteDatatypeMismatchException expected) {
      assertThat(expected).hasRootCauseInstanceOf(com.almworks.sqlite4java.SQLiteException.class);
    }
  }

  @Test
  public void interruption_doesNotConcurrentlyModifyDatabase() throws Exception {
    Thread.currentThread().interrupt();
    try {
      database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    } finally {
      Thread.interrupted();
    }
    ShadowSQLiteConnection.reset();
  }
}
