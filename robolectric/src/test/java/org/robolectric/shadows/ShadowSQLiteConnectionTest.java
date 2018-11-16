package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowSQLiteConnection.convertSQLWithLocalizedUnicodeCollator;

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
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowSQLiteConnectionTest {
  private SQLiteDatabase database;
  private File databasePath;
  private long ptr;
  private SQLiteConnection conn;
  private ShadowSQLiteConnection.Connections CONNECTIONS;
  
  @Before
  public void setUp() throws Exception {
    database = createDatabase("database.db");
    SQLiteStatement createStatement = database.compileStatement(
        "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement.execute();
    conn = getSQLiteConnection(database);
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
    assertThat(conn.isOpen()).named("open").isTrue();
  }
    
  @Test
  public void nativeClose_closesConnection() {
    ShadowSQLiteConnection.nativeClose(ptr);
    assertThat(conn.isOpen()).named("open").isFalse();
  }
    
  @Test
  public void reset_closesConnection() {
    ShadowSQLiteConnection.reset();
    assertThat(conn.isOpen()).named("open").isFalse();
  }

  @Test
  public void reset_clearsConnectionCache() {
    final Map<Long, SQLiteConnection> connectionsMap = ReflectionHelpers.getField(CONNECTIONS, "connectionsMap");

    assertThat(connectionsMap).named("connections before").isNotEmpty();
    ShadowSQLiteConnection.reset();

    assertThat(connectionsMap).named("connections after").isEmpty();
  }
  
  @Test
  public void reset_clearsStatementCache() {
    final Map<Long, SQLiteStatement> statementsMap = ReflectionHelpers.getField(CONNECTIONS, "statementsMap");

    assertThat(statementsMap).named("statements before").isNotEmpty();
    ShadowSQLiteConnection.reset();

    assertThat(statementsMap).named("statements after").isEmpty();
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
      assertThat(expected)
          .hasCauseThat()
          .hasCauseThat()
          .isInstanceOf(com.almworks.sqlite4java.SQLiteException.class);
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

  @Test
  public void test_setUseInMemoryDatabase() throws Exception {
    assertThat(conn.isMemoryDatabase()).isFalse();
    ShadowSQLiteConnection.setUseInMemoryDatabase(true);
    SQLiteDatabase inMemoryDb = createDatabase("in_memory.db");
    SQLiteConnection inMemoryConn = getSQLiteConnection(inMemoryDb);
    assertThat(inMemoryConn.isMemoryDatabase()).isTrue();
    inMemoryDb.close();
  }

  private SQLiteDatabase createDatabase(String filename) {
    databasePath = ApplicationProvider.getApplicationContext().getDatabasePath(filename);
    databasePath.getParentFile().mkdirs();
    return SQLiteDatabase.openOrCreateDatabase(databasePath.getPath(), null);
  }

  private SQLiteConnection getSQLiteConnection(SQLiteDatabase database) {
    ptr = ShadowSQLiteConnection.nativeOpen(databasePath.getPath(), 0, "test connection", false, false).longValue();
    CONNECTIONS = ReflectionHelpers.getStaticField(ShadowSQLiteConnection.class, "CONNECTIONS");
    return CONNECTIONS.getConnection(ptr);
  }
}
