package org.robolectric.shadows;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteGlobal;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteDatabaseTest {
  private SQLiteDatabase database;
  private List<SQLiteDatabase> openDatabases = new ArrayList<>();
  private static final String ANY_VALID_SQL = "SELECT 1";
  private File databasePath;

  @Before
  public void setUp() throws Exception {
    databasePath = ApplicationProvider.getApplicationContext().getDatabasePath("database.db");
    databasePath.getParentFile().mkdirs();

    database = openOrCreateDatabase(databasePath);
    database.execSQL(
        "CREATE TABLE table_name (\n"
            + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "  first_column VARCHAR(255),\n"
            + "  second_column BINARY,\n"
            + "  name VARCHAR(255),\n"
            + "  big_int INTEGER\n"
            + ");");

    database.execSQL(
        "CREATE TABLE rawtable (\n"
            + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "  first_column VARCHAR(255),\n"
            + "  second_column BINARY,\n"
            + "  name VARCHAR(255),\n"
            + "  big_int INTEGER\n"
            + ");");

    database.execSQL(
        "CREATE TABLE exectable (\n"
            + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "  first_column VARCHAR(255),\n"
            + "  second_column BINARY,\n"
            + "  name VARCHAR(255),\n"
            + "  big_int INTEGER\n"
            + ");");

    database.execSQL(
        "CREATE TABLE blob_table (\n" + "  id INTEGER PRIMARY KEY,\n" + "  blob_col BLOB\n" + ");");

    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[] {1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("rawtable", null, values);
    ////////////////////////////////////////////////
    String stringColumnValue2 = "column_value2";
    byte[] byteColumnValue2 = new byte[] {4, 5, 6};
    ContentValues values2 = new ContentValues();

    values2.put("first_column", stringColumnValue2);
    values2.put("second_column", byteColumnValue2);

    database.insert("rawtable", null, values2);
  }

  @After
  public void tearDown() {
    for (SQLiteDatabase openDatabase : openDatabases) {
      openDatabase.close();
    }
  }

  @Test
  public void testInsertAndQuery() {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[] {1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("table_name", null, values);

    Cursor cursor =
        database.query(
            "table_name",
            new String[] {"second_column", "first_column"},
            null,
            null,
            null,
            null,
            null);

    assertThat(cursor.moveToFirst()).isTrue();

    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);

    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
    cursor.close();
  }

  @Test
  public void testInsertAndRawQuery() {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[] {1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("table_name", null, values);

    Cursor cursor =
        database.rawQuery("select second_column, first_column from" + " table_name", null);

    assertThat(cursor.moveToFirst()).isTrue();

    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);

    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
    cursor.close();
  }

  @Test(expected = android.database.SQLException.class)
  public void testInsertOrThrowWithSQLException() {
    ContentValues values = new ContentValues();
    values.put("id", 1);

    database.insertOrThrow("table_name", null, values);
    database.insertOrThrow("table_name", null, values);
  }

  @Test
  public void testInsertOrThrow() {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[] {1, 2, 3};
    ContentValues values = new ContentValues();
    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);
    database.insertOrThrow("table_name", null, values);

    Cursor cursor =
        database.rawQuery("select second_column, first_column from" + " table_name", null);
    assertThat(cursor.moveToFirst()).isTrue();
    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);
    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
    cursor.close();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRawQueryThrowsIndex0NullException() {
    database.rawQuery(
        "select second_column, first_column from rawtable" + " WHERE `id` = ?",
        new String[] {null});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRawQueryThrowsIndex0NullException2() {
    database.rawQuery("select second_column, first_column from rawtable", new String[] {null});
  }

  @Test
  public void testRawQueryCountWithOneArgument() {
    Cursor cursor =
        database.rawQuery(
            "select second_column, first_column from rawtable WHERE" + " `id` = ?",
            new String[] {"1"});
    assertThat(cursor.getCount()).isEqualTo(1);
    cursor.close();
  }

  @Test
  public void testRawQueryCountWithNullArgs() {
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable", null);
    assertThat(cursor.getCount()).isEqualTo(2);
    cursor.close();
  }

  @Test
  public void testRawQueryCountWithEmptyArguments() {
    Cursor cursor =
        database.rawQuery(
            "select second_column, first_column" + " from" + " rawtable", new String[] {});
    assertThat(cursor.getCount()).isEqualTo(2);
    cursor.close();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenArgumentsDoNotMatchQuery() {
    database.rawQuery("select second_column, first_column from rawtable", new String[] {"1"});
  }

  @Test
  public void testInsertWithException() {
    ContentValues values = new ContentValues();

    assertEquals(-1, database.insert("table_that_doesnt_exist", null, values));
  }

  @Test
  public void testEmptyTable() {
    Cursor cursor =
        database.query(
            "table_name",
            new String[] {"second_column", "first_column"},
            null,
            null,
            null,
            null,
            null);

    assertThat(cursor.moveToFirst()).isFalse();
    cursor.close();
  }

  @Test
  public void testInsertRowIdGeneration() {
    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long id = database.insert("table_name", null, values);

    assertThat(id).isNotEqualTo(0L);
  }

  @Test
  public void testInsertKeyGeneration() {
    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long key =
        database.insertWithOnConflict("table_name", null, values, SQLiteDatabase.CONFLICT_IGNORE);

    assertThat(key).isNotEqualTo(0L);
  }

  @Test
  public void testInsertDuplicatedKeyGeneration() {
    ContentValues values = new ContentValues();
    values.put("id", 123);
    values.put("name", "Chuck");

    long firstKey =
        database.insertWithOnConflict("table_name", null, values, SQLiteDatabase.CONFLICT_IGNORE);

    assertThat(firstKey).isEqualTo(123L);

    long duplicateKey =
        database.insertWithOnConflict("table_name", null, values, SQLiteDatabase.CONFLICT_IGNORE);

    assertThat(duplicateKey).isEqualTo(-1L);
  }

  @Test
  public void testInsertEmptyBlobArgument() {
    ContentValues emptyBlobValues = new ContentValues();
    emptyBlobValues.put("id", 1);
    emptyBlobValues.put("blob_col", new byte[] {});

    ContentValues nullBlobValues = new ContentValues();
    nullBlobValues.put("id", 2);
    nullBlobValues.put("blob_col", (byte[]) null);

    long key =
        database.insertWithOnConflict(
            "blob_table", null, emptyBlobValues, SQLiteDatabase.CONFLICT_FAIL);
    assertThat(key).isNotEqualTo(0L);
    key =
        database.insertWithOnConflict(
            "blob_table", null, nullBlobValues, SQLiteDatabase.CONFLICT_FAIL);
    assertThat(key).isNotEqualTo(0L);

    Cursor cursor =
        database.query("blob_table", new String[] {"blob_col"}, "id=1", null, null, null, null);
    try {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getBlob(cursor.getColumnIndexOrThrow("blob_col"))).isNotNull();
    } finally {
      cursor.close();
    }

    cursor =
        database.query("blob_table", new String[] {"blob_col"}, "id=2", null, null, null, null);
    try {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getBlob(cursor.getColumnIndexOrThrow("blob_col"))).isNull();
    } finally {
      cursor.close();
    }
  }

  @Test
  public void testUpdate() {
    addChuck();

    assertThat(updateName(1234L, "Buster")).isEqualTo(1);

    Cursor cursor =
        database.query("table_name", new String[] {"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);

    assertIdAndName(cursor, 1234L, "Buster");
    cursor.close();
  }

  @Test
  public void testUpdateNoMatch() {
    addChuck();

    assertThat(updateName(5678L, "Buster")).isEqualTo(0);

    Cursor cursor =
        database.query("table_name", new String[] {"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);

    assertIdAndName(cursor, 1234L, "Chuck");
    cursor.close();
  }

  @Test
  public void testUpdateAll() {
    addChuck();
    addJulie();

    assertThat(updateName("Belvedere")).isEqualTo(2);

    Cursor cursor =
        database.query("table_name", new String[] {"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);

    assertIdAndName(cursor, 1234L, "Belvedere");
    assertThat(cursor.moveToNext()).isTrue();

    assertIdAndName(cursor, 1235L, "Belvedere");
    assertThat(cursor.isLast()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.isAfterLast()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
    cursor.close();
  }

  @Test
  public void testDelete() {
    addChuck();

    int deleted = database.delete("table_name", "id=1234", null);
    assertThat(deleted).isEqualTo(1);

    assertEmptyDatabase();
  }

  @Test
  public void testDeleteNoMatch() {
    addChuck();

    int deleted = database.delete("table_name", "id=5678", null);
    assertThat(deleted).isEqualTo(0);

    assertNonEmptyDatabase();
  }

  @Test
  public void testDeleteAll() {
    addChuck();
    addJulie();

    int deleted = database.delete("table_name", "1", null);
    assertThat(deleted).isEqualTo(2);

    assertEmptyDatabase();
  }

  @Test
  public void testExecSQL() {
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);
    cursor.close();

    cursor = database.rawQuery("SELECT * FROM table_name", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(1234);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Chuck");
    cursor.close();
  }

  @Test
  public void testExecSQLParams() {
    database.execSQL(
        "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name`"
            + " VARCHAR , `lastUsed` INTEGER DEFAULT 0 , "
            + " UNIQUE (`name`)) ",
        new Object[] {});
    database.execSQL(
        "INSERT INTO `routine` (`name`" + " ,`lastUsed`" + " ) VALUES" + " (?,?)",
        new Object[] {"Leg Press", 0});
    database.execSQL(
        "INSERT INTO `routine` (`name`" + " ,`lastUsed`" + " ) VALUES" + " (?,?)",
        new Object[] {"Bench" + " Press", 1});

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM `routine`", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(2);
    cursor.close();

    cursor = database.rawQuery("SELECT `id`, `name` ,`lastUsed` FROM `routine`", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(1);
    assertThat(cursor.getInt(cursor.getColumnIndex("lastUsed"))).isEqualTo(0);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Leg Press");

    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(2);
    assertThat(cursor.getInt(cursor.getColumnIndex("lastUsed"))).isEqualTo(1);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Bench Press");
    cursor.close();
  }

  @Test(expected = SQLiteException.class)
  public void execSqlShouldThrowOnBadQuery() {
    database.execSQL("INSERT INTO table_name;"); // invalid SQL
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecSQLExceptionParametersWithoutArguments() {
    database.execSQL("insert into exectable (first_column) values (?);", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecSQLWithNullBindArgs() {
    database.execSQL("insert into exectable (first_column) values ('sdfsfs');", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecSQLTooManyBindArguments() {
    database.execSQL(
        "insert into exectable (first_column) values" + " ('kjhk');", new String[] {"xxxx"});
  }

  @Test
  public void testExecSQLWithEmptyBindArgs() {
    database.execSQL("insert into exectable (first_column) values ('eff');", new String[] {});
  }

  @Test
  public void testExecSQLInsertNull() {
    String name = "nullone";

    database.execSQL(
        "insert into exectable (first_column, name)" + " values" + " (?,?);",
        new String[] {null, name});

    Cursor cursor =
        database.rawQuery(
            "select * from exectable WHERE" + " `name`" + " = ?", new String[] {name});
    cursor.moveToFirst();
    int firstIndex = cursor.getColumnIndex("first_column");
    int nameIndex = cursor.getColumnIndex("name");
    assertThat(cursor.getString(nameIndex)).isEqualTo(name);
    assertThat(cursor.getString(firstIndex)).isEqualTo(null);
    cursor.close();
  }

  @Test
  public void testExecSQLAutoIncrementSQLite() {
    database.execSQL(
        "CREATE TABLE auto_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name" + " VARCHAR(255));");

    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long key = database.insert("auto_table", null, values);
    assertThat(key).isNotEqualTo(0L);

    long key2 = database.insert("auto_table", null, values);
    assertThat(key2).isNotEqualTo(key);
  }

  @Test(expected = IllegalStateException.class)
  public void testClose() {
    database.close();

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
  }

  @Test
  public void testIsOpen() {
    assertThat(database.isOpen()).isTrue();
    database.close();
    assertThat(database.isOpen()).isFalse();
  }

  @Test
  public void shouldStoreGreatBigHonkingIntegersCorrectly() {
    database.execSQL("INSERT INTO table_name(big_int) VALUES(1234567890123456789);");
    Cursor cursor =
        database.query("table_name", new String[] {"big_int"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertEquals(1234567890123456789L, cursor.getLong(0));
    cursor.close();
  }

  @Test
  public void testSuccessTransaction() {
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    database.setTransactionSuccessful();
    database.endTransaction();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);
    cursor.close();
  }

  @Test
  public void testFailureTransaction() {
    database.beginTransaction();

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    final String select = "SELECT COUNT(*) FROM table_name";

    Cursor cursor = database.rawQuery(select, null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);
    cursor.close();

    database.endTransaction();

    cursor = database.rawQuery(select, null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(0);
    cursor.close();
  }

  @Test
  public void testSuccessNestedTransaction() {
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(12345, 'Julie');");
    database.setTransactionSuccessful();
    database.endTransaction();
    database.setTransactionSuccessful();
    database.endTransaction();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(2);
    cursor.close();
  }

  @Test
  public void testFailureNestedTransaction() {
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(12345, 'Julie');");
    database.endTransaction();
    database.setTransactionSuccessful();
    database.endTransaction();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(0);
    cursor.close();
  }

  @Test
  public void testTransactionAlreadySuccessful() {
    database.beginTransaction();
    database.setTransactionSuccessful();
    try {
      database.setTransactionSuccessful();
      fail("didn't receive the expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("transaction");
      assertThat(e.getMessage()).contains("successful");
    } finally {
      database.endTransaction();
    }
  }

  @Test
  public void testInTransaction() {
    assertThat(database.inTransaction()).isFalse();
    database.beginTransaction();
    assertThat(database.inTransaction()).isTrue();
    database.endTransaction();
    assertThat(database.inTransaction()).isFalse();
  }

  @Test
  public void testReplace() {
    long id = addChuck();
    assertThat(id).isNotEqualTo(-1L);

    ContentValues values = new ContentValues();
    values.put("id", id);
    values.put("name", "Norris");

    long replaceId = database.replace("table_name", null, values);
    assertThat(replaceId).isEqualTo(id);

    String query = "SELECT name FROM table_name where id = " + id;
    Cursor cursor = executeQuery(query);

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Norris");
    cursor.close();
  }

  @Test
  public void testReplaceIsReplacing() {
    final String query = "SELECT first_column FROM table_name WHERE id = ";
    String stringValueA = "column_valueA";
    String stringValueB = "column_valueB";
    long id = 1;

    ContentValues valuesA = new ContentValues();
    valuesA.put("id", id);
    valuesA.put("first_column", stringValueA);

    ContentValues valuesB = new ContentValues();
    valuesB.put("id", id);
    valuesB.put("first_column", stringValueB);

    long firstId = database.replaceOrThrow("table_name", null, valuesA);
    Cursor firstCursor = executeQuery(query + firstId);
    assertThat(firstCursor.moveToNext()).isTrue();
    long secondId = database.replaceOrThrow("table_name", null, valuesB);
    Cursor secondCursor = executeQuery(query + secondId);
    assertThat(secondCursor.moveToNext()).isTrue();

    assertThat(firstId).isEqualTo(id);
    assertThat(secondId).isEqualTo(id);
    assertThat(firstCursor.getString(0)).isEqualTo(stringValueA);
    assertThat(secondCursor.getString(0)).isEqualTo(stringValueB);
    firstCursor.close();
    secondCursor.close();
  }

  @Test
  public void shouldCreateDefaultCursorFactoryWhenNullFactoryPassedToRawQuery() {
    Cursor cursor = database.rawQueryWithFactory(null, ANY_VALID_SQL, null, null);
    cursor.close();
  }

  @Test
  public void shouldCreateDefaultCursorFactoryWhenNullFactoryPassedToQuery() {
    Cursor cursor =
        database.queryWithFactory(
            null, false, "table_name", null, null, null, null, null, null, null);
    cursor.close();
  }

  @Test
  public void shouldOpenExistingDatabaseFromFileSystemIfFileExists() {
    database.close();

    SQLiteDatabase db =
        SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, OPEN_READWRITE);
    Cursor c = db.rawQuery("select * from rawtable", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(2);
    c.close();
    assertThat(db.isOpen()).isTrue();
    db.close();
    assertThat(db.isOpen()).isFalse();

    SQLiteDatabase reopened =
        SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, OPEN_READWRITE);
    assertThat(reopened).isNotSameInstanceAs(db);
    assertThat(reopened.isOpen()).isTrue();
    reopened.close();
  }

  @Test(expected = SQLiteException.class)
  public void shouldThrowIfFileDoesNotExist() {
    File testDb = new File("/i/do/not/exist");
    assertThat(testDb.exists()).isFalse();
    SQLiteDatabase.openOrCreateDatabase(testDb.getAbsolutePath(), null);
  }

  @Test
  public void shouldUseInMemoryDatabaseWhenCallingCreate() {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    assertThat(db.isOpen()).isTrue();
    assertThat(db.getPath()).isEqualTo(":memory:");
    db.close();
  }

  @Test
  public void shouldSetAndGetVersion() {
    assertThat(database.getVersion()).isEqualTo(0);
    database.setVersion(20);
    assertThat(database.getVersion()).isEqualTo(20);
  }

  @Test
  public void testTwoConcurrentDbConnections() {
    SQLiteDatabase db1 = openOrCreateDatabase("db1");
    SQLiteDatabase db2 = openOrCreateDatabase("db2");

    db1.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    db2.execSQL("CREATE TABLE bar(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");

    ContentValues d1 = new ContentValues();
    d1.put("data", "d1");

    ContentValues d2 = new ContentValues();
    d2.put("data", "d2");

    db1.insert("foo", null, d1);
    db2.insert("bar", null, d2);

    Cursor c = db1.rawQuery("select * from foo", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d1");
    c.close();

    c = db2.rawQuery("select * from bar", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d2");
    c.close();
  }

  @Test(expected = SQLiteException.class)
  public void testQueryThrowsSQLiteException() {
    SQLiteDatabase db1 = openOrCreateDatabase("db1");
    db1.query("FOO", null, null, null, null, null, null);
  }

  @Test(expected = SQLiteException.class)
  public void testShouldThrowSQLiteExceptionIfOpeningNonexistentDatabase() {
    SQLiteDatabase.openDatabase("/does/not/exist", null, OPEN_READWRITE);
  }

  @Test
  public void testCreateAndDropTable() throws Exception {
    SQLiteDatabase db = openOrCreateDatabase("db1");
    db.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    Cursor c = db.query("FOO", null, null, null, null, null, null);
    assertThat(c).isNotNull();
    c.close();
    db.close();
    db = openOrCreateDatabase("db1");
    db.execSQL("DROP TABLE IF EXISTS foo;");
    try {
      c = db.query("FOO", null, null, null, null, null, null);
      fail("expected no such table exception");
    } catch (SQLiteException e) {
      // TODO
    }
    db.close();
  }

  @Test
  public void testCreateAndAlterTable() {
    SQLiteDatabase db = openOrCreateDatabase("db1");
    db.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    Cursor c = db.query("FOO", null, null, null, null, null, null);
    assertThat(c).isNotNull();
    c.close();
    db.close();
    db = openOrCreateDatabase("db1");
    db.execSQL("ALTER TABLE foo ADD COLUMN more TEXT NULL;");
    c = db.query("FOO", null, null, null, null, null, null);
    assertThat(c).isNotNull();
    int moreIndex = c.getColumnIndex("more");
    assertThat(moreIndex).isAtLeast(0);
    c.close();
  }

  @Test
  public void testDataInMemoryDatabaseIsPersistentAfterClose() {
    SQLiteDatabase db1 = openOrCreateDatabase("db1");
    db1.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    ContentValues d1 = new ContentValues();
    d1.put("data", "d1");
    db1.insert("foo", null, d1);
    db1.close();

    SQLiteDatabase db2 = openOrCreateDatabase("db1");
    Cursor c = db2.rawQuery("select * from foo", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d1");
    c.close();
  }

  @Test
  public void testRawQueryWithFactoryAndCancellationSignal() {
    CancellationSignal signal = new CancellationSignal();

    Cursor cursor =
        database.rawQueryWithFactory(null, "select * from" + " table_name", null, null, signal);
    assertThat(cursor).isNotNull();
    assertThat(cursor.getColumnCount()).isEqualTo(5);
    assertThat(cursor.isClosed()).isFalse();

    signal.cancel();

    try {
      cursor.moveToNext();
      fail("did not get cancellation signal");
    } catch (OperationCanceledException e) {
      // expected
    }
    cursor.close();
  }

  @Test
  public void testRawQueryWithCommonTableExpression() {
    try (Cursor cursor =
        database.rawQuery(
            "WITH RECURSIVE\n"
                + "  cnt(x) AS (VALUES(1) UNION ALL SELECT x+1 FROM cnt WHERE x<100)\n"
                + "SELECT COUNT(*) FROM cnt;",
            null)) {
      assertThat(cursor).isNotNull();
      assertThat(cursor.moveToNext()).isTrue();
      assertThat(cursor.getCount()).isEqualTo(1);
      assertThat(cursor.isNull(0)).isFalse();
      assertThat(cursor.getLong(0)).isEqualTo(100);
    }
  }

  @Test
  public void shouldBeAbleToBeUsedFromDifferentThread() {
    final CountDownLatch sync = new CountDownLatch(1);
    final Throwable[] error = {null};

    new Thread() {
      @Override
      public void run() {
        try (Cursor c = executeQuery("select * from table_name")) {
        } catch (Throwable e) {
          e.printStackTrace();
          error[0] = e;
        } finally {
          sync.countDown();
        }
      }
    }.start();

    try {
      sync.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertThat(error[0]).isNull();
  }

  private Cursor executeQuery(String query) {
    return database.rawQuery(query, null);
  }

  private long addChuck() {
    return addPerson(1234L, "Chuck");
  }

  private long addJulie() {
    return addPerson(1235L, "Julie");
  }

  private long addPerson(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("id", id);
    values.put("name", name);
    return database.insert("table_name", null, values);
  }

  private int updateName(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, "id=" + id, null);
  }

  private int updateName(String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, null, null);
  }

  private void assertIdAndName(Cursor cursor, long id, String name) {
    long idValueFromDatabase;
    String stringValueFromDatabase;

    idValueFromDatabase = cursor.getLong(0);
    stringValueFromDatabase = cursor.getString(1);
    assertThat(idValueFromDatabase).isEqualTo(id);
    assertThat(stringValueFromDatabase).isEqualTo(name);
  }

  private void assertEmptyDatabase() {
    Cursor cursor =
        database.query("table_name", new String[] {"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.isClosed()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
    cursor.close();
  }

  private void assertNonEmptyDatabase() {
    Cursor cursor =
        database.query("table_name", new String[] {"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isNotEqualTo(0);
    cursor.close();
  }

  @Test
  public void shouldAlwaysReturnCorrectIdFromInsert() {
    database.execSQL(
        "CREATE TABLE table_A (\n"
            + "  _id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "  id INTEGER DEFAULT 0\n"
            + ");");

    database.execSQL("CREATE VIRTUAL TABLE new_search USING fts3 (id);");

    database.execSQL(
        "CREATE TRIGGER t1 AFTER INSERT ON table_A WHEN new.id=0 BEGIN UPDATE"
            + " table_A SET id=-new._id WHERE _id=new._id AND id=0; END;");
    database.execSQL(
        "CREATE TRIGGER t2 AFTER INSERT ON table_A BEGIN INSERT INTO new_search"
            + " (id) VALUES (new._id); END;");
    database.execSQL(
        "CREATE TRIGGER t3 BEFORE UPDATE ON table_A BEGIN DELETE FROM new_search"
            + " WHERE id MATCH old._id; END;");
    database.execSQL(
        "CREATE TRIGGER t4 AFTER UPDATE ON table_A BEGIN INSERT INTO new_search"
            + " (id) VALUES (new._id); END;");

    long[] returnedIds =
        new long[] {
          database.insert("table_A", "id", new ContentValues()),
          database.insert("table_A", "id", new ContentValues())
        };

    Cursor c = database.query("table_A", new String[] {"_id"}, null, null, null, null, null);
    assertThat(c).isNotNull();

    long[] actualIds = new long[c.getCount()];
    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
      actualIds[c.getPosition()] = c.getLong(c.getColumnIndexOrThrow("_id"));
    }
    c.close();

    assertThat(returnedIds).isEqualTo(actualIds);
  }

  @Test
  public void shouldCorrectlyReturnNullValues() {
    database.execSQL(
        "CREATE TABLE null_test (col_int INTEGER, col_text TEXT, col_real REAL,"
            + " col_blob BLOB)");

    ContentValues data = new ContentValues();
    data.putNull("col_int");
    data.putNull("col_text");
    data.putNull("col_real");
    data.putNull("col_blob");
    assertThat(database.insert("null_test", null, data)).isAtLeast(0L);

    Cursor nullValuesCursor = database.query("null_test", null, null, null, null, null, null);
    nullValuesCursor.moveToFirst();
    final int colsCount = 4;
    for (int i = 0; i < colsCount; i++) {
      assertThat(nullValuesCursor.getType(i)).isEqualTo(Cursor.FIELD_TYPE_NULL);
      assertThat(nullValuesCursor.getString(i)).isNull();
    }
    assertThat(nullValuesCursor.getBlob(3)).isNull();
    nullValuesCursor.close();
  }

  @Test
  public void sqliteGlobal_defaults() {
    assertThat(SQLiteGlobal.getDefaultSyncMode()).isEqualTo("OFF");
    assertThat(SQLiteGlobal.getWALSyncMode()).isEqualTo("OFF");
    assertThat(SQLiteGlobal.getDefaultJournalMode()).isEqualTo("MEMORY");
  }

  private SQLiteDatabase openOrCreateDatabase(String name) {
    return openOrCreateDatabase(ApplicationProvider.getApplicationContext().getDatabasePath(name));
  }

  private SQLiteDatabase openOrCreateDatabase(File databasePath) {
    SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
    openDatabases.add(database);
    return database;
  }
}
